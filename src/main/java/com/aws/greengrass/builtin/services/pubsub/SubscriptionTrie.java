/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.builtin.services.pubsub;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trie.
 */
public class SubscriptionTrie {

    private final Map<Integer, SubscriptionTrie> children = new ConcurrentHashMap<>();
    private final int value;
    private final boolean isWildcard;
    private final Set<Object> callbacks;

    /**
     * Construct.
     */
    public SubscriptionTrie() {
        this(0);
    }

    /**
     * Get callback objects.
     * @param includeWildcardChild whether to include wild cards callbacks or not
     * @return the set of callback objects.
     */
    public Set<Object> getCallbacks(boolean includeWildcardChild) {
        SubscriptionTrie wildcard = children.get((int)'*');
        if (wildcard == null) {
            return callbacks;
        }
        Set<Object> ret = new LinkedHashSet<>();
        ret.addAll(callbacks);
        ret.addAll(wildcard.getCallbacks(true));
        return ret;
    }

    private SubscriptionTrie(char value, Set<Object> callbacks) {
        this.value = value;
        this.isWildcard = value == '*';
        this.callbacks = callbacks;
    }

    private SubscriptionTrie(int value) {
        this.value = value;
        this.isWildcard = value == '*';
        this.callbacks = ConcurrentHashMap.newKeySet();
    }

    private SubscriptionTrie lookup(String topic) {
        SubscriptionTrie current = this;
        for (int i = 0; i < topic.length(); i++) {
            current = current.children.get((int) topic.charAt(i));
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Contains key.
     * @param topic topic.
     * @return true.
     */
    public boolean containsKey(String topic) {
        return lookup(topic) != null;
    }

    /**
     * Remove.
     * @param topic topic.
     * @param cb cb.
     * @return true.
     */
    public boolean remove(String topic, Object cb) {
        return remove(topic, Collections.singleton(cb));
    }

    /**
     * Remove.
     * @param topic topic.
     * @param cbs cbs.
     */
    public boolean remove(String topic, Set<Object> cbs) {
        SubscriptionTrie sub = lookup(topic);
        if (sub == null) {
            return false;
        }
        return sub.callbacks.removeAll(cbs);
    }

    /**
     * Size.
     * @return size.
     */
    public int size() {
        int[] size = { this.callbacks.size() };
        children.forEach((s, t) -> {
            size[0] += t.size();
        });
        return size[0];
    }

    /**
     * a.
     * @param topic topic.
     * @param cb cb.
     * @return true.
     */
    public boolean add(String topic, Object cb) {
        ConcurrentHashMap.KeySetView<Object, Boolean> cbs = ConcurrentHashMap.newKeySet();
        cbs.add(cb);
        put(topic, cbs);
        return true;
    }

    /**
     * Put.
     * @param topic t.
     * @param cbs cbs.
     */
    public void put(String topic, Set<Object> cbs) {
        SubscriptionTrie[] current = { this };
        topic.chars().forEachOrdered(c -> current[0] = current[0].children.computeIfAbsent(c, SubscriptionTrie::new));
        current[0].callbacks.addAll(cbs);
    }

    /**
     * Get.
     * @param topic topic.
     * @return callbacks
     */
    public Set<Object> get(String topic) {
        Map<Integer, Set<SubscriptionTrie>> positionMap = new LinkedHashMap<>();
        for (int i = 0; i < topic.length(); i++) {
            int c = topic.charAt(i);
            if (i == 0) {
                Set<SubscriptionTrie> paths = getMatchingPaths(c, c != '$');
                if (paths.isEmpty()) {
                    return Collections.emptySet();
                }
                positionMap.put(i, paths);
                continue;
            }

            Set<SubscriptionTrie> paths = positionMap.get(i - 1);

            Set<SubscriptionTrie> newPaths = new LinkedHashSet<>();
            for (Iterator<SubscriptionTrie> it = paths.iterator(); it.hasNext(); ) {
                SubscriptionTrie path = it.next();
                Set<SubscriptionTrie> childPaths = path.getMatchingPaths(c, true);
                if (childPaths.isEmpty()) {
                    it.remove();
                } else {
                    newPaths.addAll(childPaths);
                }
            }
            if (newPaths.isEmpty()) {
                return Collections.emptySet();
            }
            positionMap.put(i, newPaths);
        }

        Set<Object> cbs = new LinkedHashSet<>();
        if (topic.length() > 0) {
            positionMap.get(topic.length() - 1).forEach(s -> cbs.addAll(s.getCallbacks(true)));
        }
        return cbs;
    }

    private Set<SubscriptionTrie> getMatchingPaths(int c, boolean allowWildcard) {
        Set<SubscriptionTrie> results = new LinkedHashSet<>();
        if (allowWildcard && isWildcard) {
            results.add(this);
        }
        this.children.values().stream().filter(s -> allowWildcard && s.isWildcard || s.value == c)
                .forEachOrdered(s -> {
                    if (s.isWildcard) {
                        results.addAll(s.children.values());
                    }
                    results.add(s);
                });

        return results;
    }

    @Override
    public String toString() {
        return "Trie: " + (char)value;
    }
}
