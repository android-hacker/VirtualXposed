/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lody.virtual.service.accounts;

import android.accounts.Account;
import android.util.LruCache;
import android.util.Pair;

import com.android.internal.util.Preconditions;
import com.lody.virtual.helper.compat.ObjectsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * TokenCaches manage time limited authentication tokens in memory. 
 */
/* default */ class TokenCache {

    private static final int MAX_CACHE_CHARS = 64000;

    private static class Value {
        public final String token;
        public final long expiryEpochMillis;

        public Value(String token, long expiryEpochMillis) {
            this.token = token;
            this.expiryEpochMillis = expiryEpochMillis;
        }
    }

    private static class Key {
        public final Account account;
        public final String packageName;
        public final String tokenType;
        public final byte[] sigDigest;

        public Key(Account account, String tokenType, String packageName, byte[] sigDigest) {
            this.account = account;
            this.tokenType = tokenType;
            this.packageName = packageName;
            this.sigDigest = sigDigest;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Key) {
                Key cacheKey = (Key) o;
                return ObjectsCompat.equals(account, cacheKey.account)
                        && ObjectsCompat.equals(packageName, cacheKey.packageName)
                        && ObjectsCompat.equals(tokenType, cacheKey.tokenType)
                        && Arrays.equals(sigDigest, cacheKey.sigDigest);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return account.hashCode()
                    ^ packageName.hashCode()
                    ^ tokenType.hashCode()
                    ^ Arrays.hashCode(sigDigest);
        }
    }

    private static class TokenLruCache extends LruCache<Key, Value> {

        private class Evictor {
            private final List<Key> mKeys;

            public Evictor() {
                mKeys = new ArrayList<>();
            }

            public void add(Key k) {
                mKeys.add(k);
            }

            public void evict() {
                for (Key k : mKeys) {
                    TokenLruCache.this.remove(k);
                }
            }
        }

        /**
         * Map associated tokens with an Evictor that will manage evicting the token from the
         * cache. This reverse lookup is needed because very little information is given at token
         * invalidation time.
         */
        private HashMap<Pair<String, String>, Evictor> mTokenEvictors = new HashMap<>();
        private HashMap<Account, Evictor> mAccountEvictors = new HashMap<>();

        public TokenLruCache() {
            super(MAX_CACHE_CHARS);
        }

        @Override
        protected int sizeOf(Key k, Value v) {
            return v.token.length();
        }

        @Override
        protected void entryRemoved(boolean evicted, Key k, Value oldVal, Value newVal) {
            // When a token has been removed, clean up the associated Evictor.
            if (oldVal != null && newVal == null) {
                /*
                 * This is recursive, but it won't spiral out of control because LruCache is
                 * thread safe and the Evictor can only be removed once.
                 */
                Evictor evictor = mTokenEvictors.remove(oldVal.token);
                if (evictor != null) {
                    evictor.evict();
                }
            }
        }

        public void putToken(Key k, Value v) {
            // Prepare for removal by token string.
            Evictor tokenEvictor = mTokenEvictors.get(v.token);
            if (tokenEvictor == null) {
                tokenEvictor = new Evictor();
            }
            tokenEvictor.add(k);
            mTokenEvictors.put(new Pair<>(k.account.type, v.token), tokenEvictor);

            // Prepare for removal by associated account.
            Evictor accountEvictor = mAccountEvictors.get(k.account);
            if (accountEvictor == null) {
                accountEvictor = new Evictor();
            }
            accountEvictor.add(k);
            mAccountEvictors.put(k.account, tokenEvictor);

            // Only cache the token once we can remove it directly or by account.
            put(k, v);
        }

        public void evict(String accountType, String token) {
            Evictor evictor = mTokenEvictors.get(new Pair<>(accountType, token));
            if (evictor != null) {
                evictor.evict();
            }
            
        }

        public void evict(Account account) {
            Evictor evictor = mAccountEvictors.get(account);
            if (evictor != null) {
                evictor.evict();
            }
        }
    }

    /**
     * Map associating basic token lookup information with with actual tokens (and optionally their
     * expiration times). 
     */
    private TokenLruCache mCachedTokens = new TokenLruCache();

    /**
     * Caches the specified token until the specified expiryMillis. The token will be associated
     * with the given token type, package name, and digest of signatures.
     *
     * @param token
     * @param tokenType
     * @param packageName
     * @param sigDigest
     * @param expiryMillis
     */
    public void put(
            Account account,
            String token,
            String tokenType,
            String packageName,
            byte[] sigDigest,
            long expiryMillis) {
        Preconditions.checkNotNull(account);
        if (token == null || System.currentTimeMillis() > expiryMillis) {
            return;
        }
        Key k = new Key(account, tokenType, packageName, sigDigest);
        Value v = new Value(token, expiryMillis);
        mCachedTokens.putToken(k, v);
    }

    /**
     * Evicts the specified token from the cache. This should be called as part of a token
     * invalidation workflow.
     */
    public void remove(String accountType, String token) {
        mCachedTokens.evict(accountType, token);
    }

    public void remove(Account account) {
        mCachedTokens.evict(account);
    }

    /**
     * Gets a token from the cache if possible.
     */
    public String get(Account account, String tokenType, String packageName, byte[] sigDigest) {
        Key k = new Key(account, tokenType, packageName, sigDigest);
        Value v = mCachedTokens.get(k);
        long currentTime = System.currentTimeMillis();
        if (v != null && currentTime < v.expiryEpochMillis) {
            return v.token;
        } else if (v != null) {
            remove(account.type, v.token);
        }
        return null;
    }
}
