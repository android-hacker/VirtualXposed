/**
 *    Copyright 2017 jmpews
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

#include "thread-utils-posix.h"
#include <pthread.h>

ThreadLocalKeyList *g_thread_local_key_list = 0;

ThreadLocalKeyList *zz_posix_thread_new_thread_local_key_list() {
    ThreadLocalKeyList *keylist_tmp = (ThreadLocalKeyList *)malloc(sizeof(ThreadLocalKeyList));
    keylist_tmp->capacity = 4;
    keylist_tmp->keys = (ThreadLocalKey **)malloc(sizeof(ThreadLocalKey *) * keylist_tmp->capacity);
    if (!keylist_tmp->keys) {
        return NULL;
    }
    keylist_tmp->size = 0;
    return keylist_tmp;
}

zbool zz_posix_thread_add_thread_local_key(ThreadLocalKeyList *keylist, ThreadLocalKey *key) {
    if (!keylist)
        return FALSE;

    if (keylist->size >= keylist->capacity) {
        ThreadLocalKey **keys_tmp =
            (ThreadLocalKey **)realloc(keylist->keys, sizeof(ThreadLocalKey *) * keylist->capacity * 2);
        if (!keys_tmp)
            return FALSE;
        keylist->keys = keys_tmp;
        keylist->capacity = keylist->capacity * 2;
    }
    keylist->keys[keylist->size++] = key;
    return TRUE;
}

void zz_posix_thread_initialize_thread_local_key_list() {
    if (!g_thread_local_key_list) {
        g_thread_local_key_list = zz_posix_thread_new_thread_local_key_list();
    }
}

zpointer zz_posix_thread_new_thread_local_key_ptr() {
    if (!g_thread_local_key_list) {
        zz_posix_thread_initialize_thread_local_key_list();
    }
    ThreadLocalKey *key = (ThreadLocalKey *)malloc(sizeof(ThreadLocalKey));
    zz_posix_thread_add_thread_local_key(g_thread_local_key_list, key);

    pthread_key_create(&(key->key), NULL);
    return (zpointer)key;
}

zpointer zz_posix_thread_get_current_thread_data(zpointer key_ptr) {
    ThreadLocalKeyList *g_keys = g_thread_local_key_list;
    zsize i;

    if (!key_ptr)
        return NULL;
    for (i = 0; i < g_keys->size; i++) {
        if (g_keys->keys[i] == key_ptr)
            return (zpointer)pthread_getspecific(g_keys->keys[i]->key);
    }
    return NULL;
}

zbool zz_posix_thread_set_current_thread_data(zpointer key_ptr, zpointer data) {
    ThreadLocalKeyList *g_keys = g_thread_local_key_list;
    zsize i;

    for (i = 0; i < g_keys->size; i++) {
        if (g_keys->keys[i] == key_ptr)
            return pthread_setspecific(g_keys->keys[i]->key, data);
    }
    return FALSE;
}

long zz_posix_get_current_thread_id() { return (long)pthread_self(); }
