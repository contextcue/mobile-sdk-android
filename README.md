# mobile-sdk-android contextcue

This project is for hosting [ContextCue](https://www.contextcue.com "ContextCue") ads on your Android applications.
ContextCue is a privacy focused ad network for advertisers and publishers. Through ContextCue you can
host ads on your application without giving away your user's information and have no fear of them being
tracked. This ad sdk is written in native Android and consists of adding a dependency and component to your layouts.


## Getting Started

### Device requirements

This library supports android API 15 and above. You also need to add the INTERNET permission to your AndroidManifest.xml

```
<uses-permission android:name="android.permission.INTERNET" />
```

### Installing

To install this library add this dependency to your build.gradle

```
Give the example
```

And then put an AdView component in your layout

```
<com.contextcue.adview.AdView
        android:id="@+id/adView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:adjustViewBounds="true"
        app:siteId="29025d2d-d9b7-4251-913b-2f4b3cfdc752"
        app:slotId="XXXXXXXXX"
        app:slotWidth="320"
        app:slotHeight="50"
        />
```

The site and slot id can be retrieved by going to your ContextCue [sites](https://adstudio.contextcue.com/publisher/sites) and continuing to the desired ad unit.

The sizing can be changed to whatever you like, but we will always select an ad from these [sizes](https://support.contextcue.com/support/ad-sizing) that can fit in the dimensions you specify.

## Versioning

We use [Bintray](https://bintray.com/cpitzo/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags).
The packages are published to Bintray, JCentral, and Maven.

## Authors

* **Corey Pitzo** - *Initial work* - [corey-pitzo-cc](https://github.com/corey-pitzo-cc)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
