/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.builtin.services.pubsub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SubscriptionTrieTest {

    SubscriptionTrie trie;

    @BeforeEach
    void setup() {
        trie = new SubscriptionTrie();
    }

    @ParameterizedTest
    @MethodSource("subscriptionMatch")
    public void GIVEN_subscription_THEN_match(String subscription, List<String> topics) {
        Object cb1 = new Object();
        Object cb2 = new Object();
        trie.add(subscription, cb1);
        trie.add(subscription, cb2);
        for (String topic : topics) {
            assertThat(trie.get(topic), containsInAnyOrder(cb1, cb2));
        }
    }

    static Stream<Arguments> subscriptionMatch() {
        return Stream.of(
                arguments("foo", singletonList("foo")),
                arguments("foo/bar", singletonList("foo/bar")),
                arguments("*", asList("foo", "foo/bar", "foo/bar/baz", "a", "abc")),
                arguments("foo/*", asList("foo/bar", "foo/", "foo/bar/baz")),
                arguments("foo*", asList("foo", "foobar", "foo/bar/baz")),
                arguments("foo/*/baz", asList("foo/bar/baz", "foo/bar/bar/baz")),
                arguments("foo/*/baz/*", asList("foo//baz/", "foo/bar/baz/bat")),
                arguments("foo*baz", asList("foobaz", "foobarbaz", "foo/bar/baz")),
                arguments("$aws/things/*/shadow/*",
                        asList("$aws/things/foo/shadow/update", "$aws/things/bar/shadow/update"))
                );
    }

    @ParameterizedTest
    @MethodSource("subscriptionNotMatch")
    public void GIVEN_subscription_THEN_do_not_match(String subscription, List<String> topics) {
        Object cb1 = new Object();
        Object cb2 = new Object();
        trie.add(subscription, cb1);
        trie.add(subscription, cb2);

        for (String topic : topics) {
            assertThat(trie.get(topic), not(containsInAnyOrder(cb1, cb2)));
        }
    }

    static Stream<Arguments> subscriptionNotMatch() {
        return Stream.of(
                arguments("foo", asList("fo", "foo/bar", "abc")),
                arguments("foo/bar", asList("foo", "foo/bar/baz")),
                arguments("foo*", asList("fo", "fob", "boof")),
                arguments("foo/*/baz", asList("foo/baz", "foo", "foo/bar/bazz")),
                arguments("foo*baz", asList("fobaz", "foobazz")),
                arguments("*", asList("$aws/foo/bar", "$foo"))
        );
    }

    @Test
    public void GIVEN_subscription_WHEN_remove_topic_THEN_no_matches() {
        Object cb1 = new Object();
        Object cb2 = new Object();
        String topic = "foo";
        trie.add(topic, cb1);
        trie.add(topic, cb2);
        assertThat(trie.get(topic), containsInAnyOrder(cb1, cb2));

        assertThat("remove topic", trie.remove(topic, cb1), is(true));
        assertThat(trie.get(topic), contains(cb2));
        assertThat("remove topic", trie.remove(topic, cb2), is(true));
        assertThat(trie.get(topic), is(empty()));
    }

    @Test
    public void GIVEN_subscription_wildcard_WHEN_remove_topic_THEN_no_matches() {
        Object cb1 = new Object();
        Object cb2 = new Object();
        String topic = "foo*";
        trie.add(topic, cb1);
        trie.add(topic, cb2);
        assertThat(trie.get(topic), containsInAnyOrder(cb1, cb2));

        assertThat("remove topic", trie.remove(topic, cb1), is(true));
        assertThat(trie.get(topic), contains(cb2));
        assertThat("remove topic", trie.remove(topic, cb2), is(true));
        assertThat(trie.get(topic), is(empty()));
    }
}
