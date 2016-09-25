# Tehreer-Android
Tehreer is a library which gives full control over following text related technologies.

* Bidirectional Algorithm
* OpenType Shaping Engine
* Text Typesetting
* Text / Glyph Rendering

It is a wrapper over mature C libraries, [FreeType](https://www.freetype.org), [SheenBidi](https://github.com/mta452/SheenBidi) and [SheenFigure](https://github.com/mta452/SheenFigure). So a part of the library has been written in JNI in order to access the functionality of said libraries.

## Memory Management
The classes that acheive their functionality by using some kind of native object, implement `Disposable` interface and provide a static method named `finalizable`. The `Disposable` interface provides a `dispose` method for cleaning up the memory manually. The `finalizable` method takes an object of same class as a parameter and returns a new object which automatically releases native memory when the object is no longer in use. The first approach is preferred and should be used when an object is only required within a scope. The second approach should only be used when it is not known how long the object will be in use.

## License
```
Copyright (C) 2016 Muhammad Tayyab Akram

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
