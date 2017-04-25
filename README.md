PubMatic SDK for Android
========================

The PubMatic Android SDK makes it easy to incorporate ads into android applications. SDK is completely modular, which provides the following benefits:

- You can integrate with either single or multiple ad formats at same time.
- Considerably reduces SDK size

Supported Ad formats
--------
* Banner
* Interstitial
* Native

License
-------
See the LICENSE file.

## Installation
PubMatic SDK supports multiple methods for installing the library in a project.

#### Integrate via central repository

To integrate PubMatic SDK into your gradle based project via central repository, specify it in your `build.gradle`:

PubMatic SDK is published in JitPack central repository. 

Users of this SDK will need to add the jitpack.io repository in the build.gradle file of the root project.

````java 
allprojects {
    repositories {
        jcenter()
        maven {
            url "https://jitpack.io"
        }
    }
}
````

And, Need to add the following code in the build.gradle of the application module:
````java 
compile ‘com.github.PubMatic.pubmatic-sdk-android:common-sdk:5.2.0’
compile ‘com.github.PubMatic.pubmatic-sdk-android:banner-sdk:5.2.0’
compile ‘com.github.PubMatic.pubmatic-sdk-android:native-sdk:5.2.0’
````
Above snippet is in form of ‘com.github.User.Repo:library:releasetag’
Where Pubmatic has separate SDK library for banner & native ad. User has an option to include the SDK based on the requirement. 

It is mandatory to include common-sdk along with banner and/or native sdk. 

5.2.0 is a release version of PubMatic SDK. It can be changed as per the requirement. 

User can also choose an option to always get the “tip” of the master branch by using:

_compile ‘com.github.user.Repo:library:-SNAPSHOT’_

For example:
````java 
compile 'com.github.PubMatic.pubmatic-sdk-android:common-sdk:-SNAPSHOT'
compile 'com.github.PubMatic.pubmatic-sdk-android:banner-sdk:-SNAPSHOT'
compile 'com.github.PubMatic.pubmatic-sdk-android:native-sdk:-SNAPSHOT'
````

#### Download the source from github

PubMatic SDK is available as Open Source project for download from GitHub. It can be downloaded & used in the android application.

Other installation methods and integration guidelines are provided at [http://developer.pubmatic.com/documentation/introduction](http://developer.pubmatic.com/documentation/introduction)
