/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#pragma once

#include "CoreClasses.h"
#include "Hybrid.h"
#include "Registration.h"

#include <functional>

namespace facebook {
namespace jni {

struct JRunnable : public JavaClass<JRunnable> {
  static auto constexpr kJavaDescriptor = "Ljava/lang/Runnable;";
};

struct JNativeRunnable : public HybridClass<JNativeRunnable, JRunnable> {
 public:
  static auto constexpr kJavaDescriptor = "Lcom/facebook/jni/NativeRunnable;";

  JNativeRunnable(std::function<void()>&& runnable) : runnable_(std::move(runnable)) {}

  static void OnLoad() {
    registerHybrid({
        makeNativeMethod("run", JNativeRunnable::run),
      });
  }

  void run() {
    runnable_();
  }

 private:
  std::function<void()> runnable_;
};


} // namespace jni
} // namespace facebook
