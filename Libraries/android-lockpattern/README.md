# android-lockpattern

* Version: 3.0.3

Feel free to contact us at:

* [Homepage](http://www.haibison.com)
* E-mails:
    + haibisonapps[at]gmail.com

In short, you can use this library in your closed source/ commercial apps with
or without our knowledge. Hope you enjoy it  :-)


# CREDITS

We sincerely thank all of our friends -- who have been contributing to this
project. We hope this project will be always useful for everyone.

* C
* [Steven Byle](http://stackoverflow.com/users/1507439/steven-byle)
* Thomas Breitbach
* Yan Cheng Cheok (Project Admin of [JStock](http://jstock.sourceforge.net/))
* And others.


# HISTORY

* Version 3.0.3:
    + *Release:* December 18, 2013
    + Optimize code.

* Version 3.0.2:
    + *Release:* December 18, 2013
    + Add theme light with dark action bar (for API 14+).

* Version 3.0.1:
    + *Release:* October 10, 2013
    + Use default icon set (from AOSP).

* Version 3.0:
    + *Release:* September 15, 2013
    + Upgrade IEncrypter.

* Version 2.9
    + *Release:* August 11, 2013
    + Fix dialog themes in API 11.
    + Optimize code.

* Version 2.9 beta
    + *Initialize:* July 05, 2013

* Version 2.8
    + *Release:* July 02, 2013
    + Add new extra `EXTRA_INTENT_ACTIVITY_FORGOT_PATTERN` to help the user
      recover the pattern if he/ she forgot it.
    + Change `char[] IEncrypter.encrypt(Context, char[])` to
      `char[] IEncrypter.encrypt(Context, List<Cell>)`.
    + Rename `EXTRA_OK_PENDING_INTENT`, `EXTRA_CANCELLED_PENDING_INTENT` to
      `EXTRA_PENDING_INTENT_OK` and `EXTRA_PENDING_INTENT_CANCELLED`.
    + Optimize code.
    + Some minor changes...

* Version 2.8 beta
    + *Initialize:* June 20, 2013

* Version 2.7
    + *Release:* June 20, 2013
    + Add new action `ACTION_VERIFY_CAPTCHA`.

* Version 2.7 beta
    + *Initialize:* May 28, 2013

* Version 2.6
    + *Release:* May 18, 2013
    + Use UPPER_CASE for `static final` fields and enums;
    + Move most of dynamic settings to `SharedPreferences`;
    + Change `IEncrypter`;

* Version 2.6 beta
    + *Initialize:* May 15, 2013

* Version 2.5.1
    + *Release:* April 15, 2013
    + Fix delivering result to `ResultReceiver`.

* Version 2.5
    + *Release:* April 15, 2013
    + Upgrade UI;
    + Add options:
        - for setting minimum wired dots in mode creating pattern;
        - for setting maximum tries and determining the number of tries that the
          user did in mode comparing patterns;
        - thanks to David Myers for his feedbacks;
    + Use fixed size for `LockPatternActivity` in large screens with dialog
      themes;
    + Add options for sending result to a `PendingIntent` and/ or
      `ResultReceiver`;
    + Fix minor bugs; optimize code;

* Version 2.5 beta
    + *Initialize:* March 18, 2013

* Version 2.4
    + *Release:* March 16, 2013
    + Merge latest code from AOSP;
    + Use action names instead of extra fields for different types of handlers:
        - `_ActionCreatePattern`
        - `_ActionComparePattern`
    + Add built-in themes: default dark and dark dialog;
    + Add stealth-mode;
    + New icon set;
    + Optimize code and UI; special thanks to
      [Steven Byle](http://stackoverflow.com/users/1507439/steven-byle):
        - <http://stackoverflow.com/a/15424636/1521536>

* Version 2.3
    + Update info: August 28, 2012
    + The
      [serious bug](https://code.google.com/p/android-lockpattern/issues/detail?id=1)
      was invalid.

* Version 2.3
    + *Release:* August 28, 2012
    + Fixed serious bug: key `_PaternSha1` is deprecated but is used to return
      the pattern;
    + Removed all fields/ methods which were deprecated in old versions;

* Version 2.2
    + *Release:* August 17, 2012
    + added: Spanish language; special thanks to C. - a kind friend who helped
      us translate the library into his mother language;

* Version 2.1
    + *Release:* July 29, 2012
    + turn off `AutoSave` by default;
    + add new method `IEncrypter.encrypt(Context, String);`
    + set method `IEncrypter.encrypt(String)` as deprecated;

* Version 2.1 beta
    + Initialization: July 21, 2012

* Version 2
    + *Release:* July 15, 2012
    + add support for encryption;

* Version 2 beta
    + Initialization: July 12, 2012

* Version 1.5.5
    + *Release:* June 22, 2012
    + set max width and height for `LockPatternView` to `400dp` for tablet;

* Version 1.5.4
    + *Release:* June 09, 2012
    + Fix bug: in mode `CreatePattern`, `LockPatternActivity` recognized wrong
      the confirmed pattern;

* Version 1.5.4 beta
    + Initialization: June 07, 2012

* Version 1.5.3
    + *Release:* June 07, 2012
    + ability to change theme in runtime;
    + save and restore controls' state after screen orientation changed;

* Version 1.5.3 beta
    + Initialization: May 21, 2012
    + make `LockPatternView`'s gravity center;

* Version 1.5.2
    + *Release:* May 21, 2012
    + in landscape mode, move button `Cancel` to bottom;

* Version 1.5.1
    + *Release:* May 21, 2012
    + set `LockPatternView`'s gravity center;

* Version 1.5
    + *Release:* May 21, 2012
    + due to
      [this bug](https://code.google.com/p/android/issues/detail?id=30622), so
      we prefix all resource names with `alp_`;
    + add layout for landscape mode;
    + update coding style:
        - prefix global fields with `m`;
        - prefix static final fields with `_`;

* Version 1.4
    + *Release:* April 29, 2012
    + change UI;
    + determine and use user's haptic feedback;

* Version 1.2
    + *Release:* March 09, 2012
    + make sure `LockPatternUtils.patternToSha1()` returns lower case string;

* Version 1.1
    + *Release:* March 08, 2012
    + fix security issue about using `SharedPreferences`;

* Version 1.0
    + *Release:* March 08, 2012
    + first release;
    + create pattern;
    + compare to existing pattern;
