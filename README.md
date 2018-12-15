![Open Source Love](https://img.shields.io/badge/Open%20Source-%E2%9D%A4-red.svg)
![GitHub](https://img.shields.io/github/license/isfaaghyth/rx-priority-scheduler.svg)
![GitHub forks](https://img.shields.io/github/forks/isfaaghyth/rx-priority-scheduler.svg)
![GitHub issues](https://img.shields.io/github/issues/isfaaghyth/rx-priority-scheduler.svg)
![GitHub pull requests](https://img.shields.io/github/issues-pr/isfaaghyth/rx-priority-scheduler.svg)
![GitHub contributors](https://img.shields.io/github/contributors/isfaaghyth/rx-priority-scheduler.svg)
![GitHub top language](https://img.shields.io/github/languages/top/isfaaghyth/rx-priority-scheduler.svg)

# rx-priority-scheduler
An RxJava scheduler that incorporates priorities in scheduling tasks

While thinking about the intersection of RxJava and Android, I realized there was no default scheduler in the library that allowed for prioritizing actions before others, similar to how [FAST](https://github.com/amitshekhariitbhu/Fast-Android-Networking)'s Priority. I decided to try and work something together and this is what I initially came up with. Some of the threading seems a bit strange and the Worker works (no pun intended) a bit differently than others, but it seems to do the trick. Gladly accepting comments/pull requests!

## Sample Usage
```kotlin
val PRIORITY_HIGH = 10
val PRIORITY_LOW = 1

val scheduler = PriorityScheduler()
Observable.just(1, 2, 3, 4, 5)
        .subscribeOn(scheduler.priority(PRIORITY_LOW))
        .subscribe()

Observable.just(6, 7, 8, 9, 10)
        .subscribeOn(scheduler.priority(PRIORITY_HIGH))
        .subscribe()
```

### Priorities

Priorties are simply ints ordered in increasing order. An action with a priority higher than another will be scheduled before (note that actions with the same priority may run in any order). Priorities may be any valid integer; you may want to define:

```kotlin
val PRIORITY_WHENEVER = Int.MIN_VALUE
```

and/or:

```kotlin
val PRIORITY_NEXT = Int.MAX_VALUE
```

### Let's go.

Step 1. Add the JitPack repository to your build file

```javascript
allprojects {
      repositories {
         ...
         maven { url 'https://jitpack.io' }
      }
}
```

Step 2. Add the dependency

```javascript
dependencies {
      implementation 'com.github.isfaaghyth:rx-priority-scheduler:1.0'
}
```

### contributor
- this project is ported from [@ronshapiro](https://github.com/ronshapiro/rxjava-priority-scheduler) (rxjava v.1)
- kotlin with rxjava2 by. [@isfaaghyth](https://github.com/isfaaghyth)


MIT
