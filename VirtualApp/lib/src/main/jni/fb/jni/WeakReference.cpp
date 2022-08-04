/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#include <fb/Environment.h>
#include <jni/WeakReference.h>

namespace facebook {
namespace jni {

WeakReference::WeakReference(jobject strongRef) :
  m_weakReference(Environment::current()->NewWeakGlobalRef(strongRef))
{
}

WeakReference::~WeakReference() {
  auto env = Environment::current();
  FBASSERTMSGF(env, "Attempt to delete jni::WeakReference from non-JNI thread");
  env->DeleteWeakGlobalRef(m_weakReference);
}

ResolvedWeakReference::ResolvedWeakReference(jobject weakRef) :
  m_strongReference(Environment::current()->NewLocalRef(weakRef))
{
}

ResolvedWeakReference::ResolvedWeakReference(const RefPtr<WeakReference>& weakRef) :
  m_strongReference(Environment::current()->NewLocalRef(weakRef->weakRef()))
{
}

ResolvedWeakReference::~ResolvedWeakReference() {
  if (m_strongReference)
    Environment::current()->DeleteLocalRef(m_strongReference);
}

} }

