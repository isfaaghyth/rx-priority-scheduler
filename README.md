# rx-priority-scheduler
An RxJava scheduler that incorporates priorities in scheduling tasks

While thinking about the intersection of RxJava and Android, I realized there was no default scheduler in the library that allowed for prioritizing actions before others, similar to how [FAST](https://github.com/amitshekhariitbhu/Fast-Android-Networking)'s Priority. I decided to try and work something together and this is what I initially came up with. Some of the threading seems a bit strange and the Worker works (no pun intended) a bit differently than others, but it seems to do the trick. Gladly accepting comments/pull requests!

## Sample Usage

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


this project is ported from [@ronshapiro](https://github.com/ronshapiro/rxjava-priority-scheduler)
