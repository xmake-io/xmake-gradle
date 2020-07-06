<div align="center">
  <a href="https://xmake.io/cn">
    <img width="200" heigth="200" src="https://tboox.org/static/img/xmake/logo256c.png">
  </a>  

  <h1>xmake-gradle</h1>

  <div>
    <a href="https://github.com/xmake-io/xmake-gradle/releases">
      <img src="https://img.shields.io/github/release/xmake-io/xmake-gradle.svg?style=flat-square" alt="Github All Releases" />
    </a>
    <a href="https://github.com/xmake-io/xmake-gradle/blob/master/LICENSE.md">
      <img src="https://img.shields.io/github/license/xmake-io/xmake-gradle.svg?colorB=f48041&style=flat-square" alt="license" />
    </a>
  </div>
  <div>
    <a href="https://www.reddit.com/r/tboox/">
      <img src="https://img.shields.io/badge/chat-on%20reddit-ff3f34.svg?style=flat-square" alt="Reddit" />
    </a>
    <a href="https://gitter.im/tboox/tboox?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge">
      <img src="https://img.shields.io/gitter/room/tboox/tboox.svg?style=flat-square&colorB=96c312" alt="Gitter" />
    </a>
    <a href="https://t.me/tbooxorg">
      <img src="https://img.shields.io/badge/chat-on%20telegram-blue.svg?style=flat-square" alt="Telegram" />
    </a>
    <a href="https://jq.qq.com/?_wv=1027&k=5hpwWFv">
      <img src="https://img.shields.io/badge/chat-on%20QQ-ff69b4.svg?style=flat-square" alt="QQ" />
    </a>
    <a href="https://xmake.io/#/zh-cn/about/sponsor">
      <img src="https://img.shields.io/badge/donate-us-orange.svg?style=flat-square" alt="Donate" />
    </a>
  </div>

  <p>A gradle plugin that integrates xmake seamlessly</p>
</div>

## 简介

xmake-gradle是一个无缝整合xmake的gradle插件。

如果你想要了解更多，请参考：

* [在线文档](https://xmake.io/#/zh-cn/getting_started)
* [项目主页](https://xmake.io/#/zh-cn/)
* [Github](https://github.com/xmake-io/xmake-gradle)
* [Gitee](https://gitee.com/tboox/xmake-gradle)
* [Gradle插件](https://plugins.gradle.org/plugin/org.tboox.gradle-xmake-plugin)

## 准备工作

我们需要先安装好对应的xmake命令行工具，关于安装说明见：[xmake](https://github.com/xmake-io/xmake)。

## 应用插件

### 通过插件DSL集成

```
plugins {
  id 'org.tboox.gradle-xmake-plugin' version '1.1.2'
}
```

### 被废弃的插件集成方式

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath 'org.tboox:gradle-xmake-plugin:1.1.2'
  }
  repositories {
    mavenCentral()
  }
}

apply plugin: "org.tboox.gradle-xmake-plugin"
```

## 配置

### 最简单的配置示例

如果我们添加`xmake.lua`文件到`projectdir/jni/xmake.lua`，那么我们只需要在build.gradle中启用生效了xmake指定下对应的JNI工程路径即可。

#### build.gradle

```
android {
    externalNativeBuild {
        xmake {
            path "jni/xmake.lua"
        }
    }
}
```

#### JNI

JNI工程结构

```
projectdir
  - src
    - main
      - java
  - jni
    - xmake.lua
    - *.cpp
```

xmake.lua:

```lua
add_rules("mode.debug", "mode.release")
target("nativelib")
    set_kind("shared")
    add_files("nativelib.cc")
```

### 更多Gradle配置说明

```
android {
    defaultConfig {
        externalNativeBuild {
            xmake {
                // 追加设置全局c编译flags
                cFlags "-DTEST"

                // 追加设置全局c++编译flags
                cppFlags "-DTEST", "-DTEST2"

                // 设置切换编译模式，与`xmake f -m debug`的配置对应，具体模式值根据自己的xmake.lua设置而定
                buildMode "debug"

                // 设置需要编译的abi列表，支持：armeabi, armeabi-v7a, arm64-v8a, x86, x86_64
                // 如果没有设置的话，我们也支持从defaultConfig.ndk.abiFilters中获取abiFilters
                abiFilters "armeabi-v7a", "arm64-v8a"

                // 设置需要被编译的targets
                // targets "xxx", "yyy"
            }
        }
    }

    externalNativeBuild {
        xmake {
            // 设置jni工程中xmake.lua根文件路径，这是必须的，不设置就不会启用jni编译
            path "jni/xmake.lua"

            // 启用详细输出，会显示完整编译命令行参数，其他值：verbose, warning, normal
            logLevel "verbose"

            // 指定c++ stl库，默认不指定会使用c++_static，其他值：c++_static/c++_shared, gnustl_static/gnustl_shared, stlport_static/stlport_shared
            stl "c++_shared"

            // 设置xmake可执行程序路径（通常不用设置）
            // program /usr/local/bin/xmake

            // 禁用stdc++库，默认是启用的
            // stdcxx false

            // 设置其他指定的ndk目录路径 （这是可选的，默认xmake会自动从$ANDROID_NDK_HOME或者`~/Library/Android/sdk/ndk-bundle`中检测）
            // 当然如果用户通过`xmake g --ndk=xxx`配置了全局设置，也会自动从这个里面检测
            // ndk "/Users/ruki/files/android-ndk-r20b/"

            // 设置ndk中sdk版本
            // sdkver 21
        }
    }
}
```

## 编译JNI

### 编译JNI并且生成APK

当`gradle-xmake-plugin`插件被应用生效后，`xmakeBuild`任务会自动注入到现有的`assemble`任务中去，自动执行jni库编译和集成。

```console
$ ./gradlew app:assembleDebug
> Task :nativelib:xmakeConfigureForArm64
> Task :nativelib:xmakeBuildForArm64
>> xmake build
[ 50%]: ccache compiling.debug nativelib.cc
[ 75%]: linking.debug libnativelib.so
[100%]: build ok!
>> install artifacts to /Users/ruki/projects/personal/xmake-gradle/nativelib/libs/arm64-v8a
> Task :nativelib:xmakeConfigureForArmv7
> Task :nativelib:xmakeBuildForArmv7
>> xmake build
[ 50%]: ccache compiling.debug nativelib.cc
[ 75%]: linking.debug libnativelib.so
[100%]: build ok!
>> install artifacts to /Users/ruki/projects/personal/xmake-gradle/nativelib/libs/armeabi-v7a
> Task :nativelib:preBuild
> Task :nativelib:assemble
> Task :app:assembleDebug
```

### 强制重建JNI

```console
$ ./gradlew nativelib:xmakeRebuild
```

## Development

### 编译插件

```console
$ ./gradlew gradle-xmake-plugin:assemble
```

### 发布插件

请参考：[https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/](https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/)

```console
$ ./gradlew gradle-xmake-plugin:publishPlugins
```

## 联系方式

* 邮箱：[waruqi@gmail.com](mailto:waruqi@gmail.com)
* 主页：[tboox.org](https://tboox.org/cn)
* 社区：[Reddit论坛](https://www.reddit.com/r/tboox/)
* 聊天：[Telegram群组](https://t.me/tbooxorg), [Gitter聊天室](https://gitter.im/tboox/tboox?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
* QQ群：343118190(满), 662147501
* 微信公众号：tboox-os

## 支持项目

xmake-gradle项目属于个人开源项目，它的发展需要您的帮助，如果您愿意支持xmake-gradle项目的开发，欢迎为其捐赠，支持它的发展。 🙏 [[支持此项目](https://opencollective.com/xmake#backer)]

<a href="https://opencollective.com/xmake#backers" target="_blank"><img src="https://opencollective.com/xmake/backers.svg?width=890"></a>

## 赞助项目

通过赞助支持此项目，您的logo和网站链接将显示在这里。[[赞助此项目](https://opencollective.com/xmake#sponsor)]

<a href="https://opencollective.com/xmake/sponsor/0/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/1/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/1/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/2/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/2/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/3/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/3/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/4/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/4/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/5/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/5/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/6/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/6/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/7/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/7/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/8/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/8/avatar.svg"></a>
<a href="https://opencollective.com/xmake/sponsor/9/website" target="_blank"><img src="https://opencollective.com/xmake/sponsor/9/avatar.svg"></a>


