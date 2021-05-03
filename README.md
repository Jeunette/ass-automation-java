# **PJSK Aegisub ASS Automation README**

This project functions to smartly generate the `.ass` files used in Aegisub
for [Project SEKAI COLORFUL STAGE!](https://pjsekai.sega.jp/) story videos.

# Important Notice

**PLEASE READ THIS DOCUMENT CAREFULLY!**

**If you used this code, PLEASE GIVE CREDIT AND LINK TO HERE
- [Jeunette / ASS-automation-java](https://github.com/Jeunette/ASS-automation-java) (
github.com/Jeunette/ASS-automation-java).** Primarily for sharing purposes.

**If you used the given color coded styles for characters, PLEASE GIVE CREDIT AND LINK
TO  [Project_SEKAI资讯站](https://space.bilibili.com/13148307/dynamic) (space.bilibili.com/13148307).** We are the
co-authors of this file.

I also have a video library (resolution 1920 x 1440) that contains most of the events story and is constantly updating.
The videos will be updated in 2 to 5 hours after the events start.

There are 2 versions of videos. One is a compressed version with the prefix `[SHANA]` attached. The other is the
original version recorded on iPad Pro 2018 12.9-inch. The compressed version doesn't have a decent color space but can
be used directly in the code without any problems and also runs fluently in Aegisub. The original version may encounter
errors in the code (ffmpeg), and, if that is the case, you can use the `compress` command then run the code with the new
compressed video. The original version is also at full color range. **This means major video sharing and streaming sites
won't support it if you burn the `.ass` subtitle file into it without adding a filter.** See *Additional Help* section "
burning `.ass` and adding filters to video" for details.

**If you choose to use my video library, PLEASE GIVE CREDIT TO MY NAME [Jeunette](https://twitter.com/Jeunette_H) AND
LINK TO [Project_SEKAI资讯站](https://space.bilibili.com/13148307/dynamic) (space.bilibili.com/13148307).** Although those
are my own recordings, those recordings mainly serve as my contribution to the translation team
@[Project_SEKAI资讯站](https://space.bilibili.com/13148307/dynamic).

Here is the link - [OneDrive](https://1drv.ms/f/s!Agy3rSeFledan2j7CXmrBhIkwwZ5).

---

# Required Third Party Programs

If you downloaded `win-full` version, you are good to go. You don't need to download an additional programs.

If you downloaded `jar` version for macOS and linux. You will need to have
the [ffmpeg](https://ffmpeg.org/download.html) & [ffprobe](https://ffmpeg.org/download.html) packages installed.

* For macOS, refer ["ffmpeg - Homebrew Formulae"](https://formulae.brew.sh/formula/ffmpeg).

* For linux, refer ["How to Install FFmpeg in Linux"](https://www.tecmint.com/install-ffmpeg-in-linux/).

## Other Necessary Programs and Files

* [Aegisub](https://github.com/Aegisub/Aegisub/releases) for editing the `.ass` files after the code runs.
* ["typeWriter v1.1 [LANG].lua"]() an Aegisub automation plugin can be found in `"help\Aegisub Automation\"` with its
  guild. This plugin can be used in Aegisub to manipulate the typewriter effect of dialogue texts in game.
* [Avidemux](http://avidemux.sourceforge.net/download.html) for burning the `.ass` files into videos and adding filters
  to the original version video from my video library. See *Additional Help* section "burning `.ass` and adding filters
  to video" for details.

---

# How to Start (Windows Only)

1. Download the most recent `[executable]-win.zip` file from
   the [release page](https://github.com/Jeunette/ASS-automation-java/releases) and unzip.

2. Double-click on `run.bat` to open the terminal.

3. Commands and usages will be automatically printed at the top of the terminal.

## Commands

    run [video_file json_file]
    ass [video_file json_file]
    compress mp4_video_file

## Notes

* `json` file is required for the code to work.

* `json` file can be found and download at [pjsek.ai](https://pjsek.ai/assets/ondemand/event_story) thanks
  to [Erik Chan](https://www.patreon.com/erikchan002).

* `run` will start the process in the main tab.

* `ass` will start the process in a new tab and allow multiple runs.

* `run` & `ass` will run with or without arguments. Runs without arguments will prompt you to enter the path of the
  files. You can drag and drop the files from File Explorer into the terminal to automatically get the path.

* `compress` is only needed if you encounter an error in running ffmpeg.

* `compress` is only confirmed to be effective on the original version `MP4` videos from my video library or iOS
  recordings in `MP4` format (unedited video otherwise it will be in `MOV` format which probably don't need to be
  compressed).

---

# Reading messages from the terminal

The terminal will print messages that will help you to continue to work on or fix problems in the `.ass` file

## TODO

`TODO` messages will display when the code detects a location transition. **This type of transitions needs to be
manually time coded. You are required to find and set the time of the line and fill the translation text for it.**

    TODO: Location transition detected (#:##:##.## - #:##:##.##).

## ATTENTION

3 types of `ATTENTION` messages will display for different situations.

In the first case, if the code detects a line that is only one character long, it will print a `ATTENTION` message. This
is a legacy feature since the algorithm detecting one character long line is unique. However, by now, this algorithm is
very stable. You may or may not need to watch out for this.

    > ATTENTION: Single width text found at frame #. Please verify!

In the second case, if the code can not directly match a section of events from the video with the section from the
correlating `json` file, it will print a `ATTENTION` message when the algorithm combined sections match with each other
with no line count difference. **You will need to inspect the sections to make sure they are correct. Both sections will
be provided. Video section is commented.**

    > Matching ImageSystem section(s) [#, ...] with Snippets section(s) [#, ...]
    > ATTENTION: Paired combination in section # (#:##:##.## - #:##:##.##).

In the third case, after everything runs, the code will print a `ATTENTION` message at the end to indicate how TODO
items found if the number is correct.

    > ATTENTION - TODO: # location transition detected.

# DISTORTION

`DISTORTION` messages will display when the code detects a jitter effect on the dialogue box. You will need to redo the
time code for some lines in the section.

    DISTORTION: Distortion transition(s) / effect(s) found in section # (#:##:##.## - #:##:##.##).

# ERROR

2 types of `ERROR` messages will display for different situations.

In the first case, if the code can not directly match a section of events from the video with the section from the
correlating `json` file, it will print a `ERROR` message when the algorithm combined sections mismatch with each other
with 1 line counts difference. **You will need to inspect and modify the sections to make them correct. Both sections
will be provided. Video section is commented.**

    > Matching ImageSystem section(s) [#, ...] with Snippets section(s) [#, ...]
    > ERROR: Single line mismatched in section # (#:##:##.## - #:##:##.##).

In the second case, if the code find an empty section and is unable to group it into a combination, it will print
a `ERROR` message when the algorithm combined sections mismatch with each other with 1 line counts difference. This
means there is a possible shift **OR** distortion in the starting or ending timestamp of a line. **You will need to
inspect and modify the sections to make them correct.**

    > ERROR: Shift (empty section) found in section # (#:##:##.## - #:##:##.##).

# CRITICAL_ERROR

2 types of `CRITICAL_ERROR` messages will display for different situations.

In the first case, if the code can not directly match a section of events from the video with the section from the
correlating `json` file, it will print a `CRITICAL_ERROR` message when the algorithm combined sections mismatch with
each other with line counts difference greater than 1. **You will need to inspect and modify the sections to make them
correct. Both sections will be provided. `json` file section is commented.**

    > Matching ImageSystem section(s) [#, ...] with Snippets section(s) [#, ...]
    > CRITICAL_ERROR: # lines mismatched in section # (#:##:##.## - #:##:##.##).

In the second case, at the end of a run, if the code detected the location transitions found in the video sections is
smaller than what is in the `.json` file sections, it will print a `CRITICAL_ERROR` message to ask you to find the
missing ones. This usually comes with other error messages and, most likely, to be major problems.

    CRITICAL_ERROR: Location Transition amount mismatched.
    Location transition discovered:     #
    Actual location transition amount:  #

# FATAL_ERROR

At the end of a run, if the code detected the location transitions found in the video sections is greater than what is
in the `.json` file sections, it will print a `FATAL_ERROR` message to ask you to find the missing ones. You should NOT
be seeing this. If this is the case, you might use the wrong `json` file otherwise send me a message please.

    FATAL_ERROR: Location Transition amount EXCEEDED.
    Location transition discovered:     #
    Actual location transition amount:  #

# Ending Prompt

Let's say what if you accidentally used the wrong `json` file? Or you want to see the messages again.

The terminal will prompt the following message to ask you whether to delete the save files. The save files work as a
save point that can load the messages and **rebuild the `.ass` file** real quick by simply re-run the `run`
or `ass` command.

**MAKE SURE YOU DON'T ACCIDENTALLY DELETE YOUR `.ASS` FILE.**

The save files are stored in `[video_name].temp` folder in the same folder with the video.

    Clean save files? (Y/N): _

---

# Modifying `settings.txt` and `[reference].txt`

There are a few things that can be modified to fit your personal use.

`settings.txt` contains some global information that is to be used in the code.

`[reference].txt` contains some personal options regarding the input video **resolution**.

---

## In `settings.txt`

First, below `[Reference File Path]`, the single and only line directs to the path of the `[reference].txt` file

    [Reference File Path]
    path_to_reference_file

Next, you have `[Sample ASS Path]` that directs to the Aegisub `.ass` file that contains the style information. The
given `untitled.ass` file is used and created by [Project_SEKAI资讯站](https://space.bilibili.com/13148307/dynamic). The
file contains **color coded styles** for each character and special instructional styles (`CAUTION`, `DELETE`,
and `screen`) related to referencing the caution and error message displayed in the terminal during running.

**PLEASE GIVE CREDIT AND LINK TO [Project_SEKAI资讯站](https://space.bilibili.com/13148307/dynamic) (
space.bilibili.com/13148307)** if you choose to use the given **color coded styles** for characters (styles other
than `CAUTION`, `DELETE`, and `screen`).

The given one only fully supports for resolution 1920 x 1440 (e.g. iPad Pro 2018 12.9-inch). Video that uses other
resolutions may result in a shift of subtitles. Editing the *Margins* settings of the styles in Aegisub *Style Editor*
can easily fix this problem. See [Styles - Aegisub Handout](https://aegi.vmoe.info/docs/3.1/Styles/).

    [Sample ASS Path]
    path_to_ass_file

Next, you have `[Default Style]` that records the default style from the `.ass` file defined in the last section. The
default style will be used in runtime to prompt you for whether to use an unrecorded `speaker_name` (see details in
later section `[Name Style]`).

    [Default Style]
    default_style_to_use_in_ass_file

Next, you have `[Screen Start Offset]` and `[Screen End Offset]` that are used for adjusting the starting and ending
timestamps of the mask on the dialogue box. These should **NOT BE MODIFIED** if you don't have any idea what these
affect.

    [Screen Start Offset]
    -0.100
 
    [Screen End Offset]
    0.000

Next, you have a list starting with `[Name Style]` and ending with `/[Name Style]`. This list is used to record the
pairing `speaker_name`s and styles. The list already contains the most of the main characters. The `speaker_name`s are
found in `json` files under the variable `"WindowDisplayName"`.

    [Name Style]
    speaker_name style
    ...
    /[Name Style]

New speakers that are not recorded in the list will be prompted to pair with the **default style** as following:

    > Actor Name - [speaker_name] - Info Not Found!
    > Use default style -  [default_style]  - for this actor name? (Y/N): _

In most situations, you would only need to enter yes (Y/y). If you want to use another style, enter no (N/n) to let the
code prompt you to enter a style name. Make sure it is **ONE_WORD**, only words of English alphabet are supported
otherwise it will not encode correctly by the terminal. If you want to add a style name that contains characters other
than English alphabet, you can manually add a line in the list and make sure you follow the
formats (`speaker_name style`, make sure there is a single space).

    > Use default style -  [default_style]  - for this actor name? (Y/N): N
    > Enter the custom style (ONE_WORD) for this actor name: 

Next, you have a list starting with `[TransitionType Description Ignore]` and ending
with `[TransitionType Description Ignore]`. This is used only for debugging purposes.

    [TransitionType Description Ignore]
    ...
    /[TransitionType Description Ignore]

If a message like the following is prompted, simply enter yes (Y/y) to ignore.

    > Transition type - [type_num] - info NOT FOUND!
    > Ignore this transition? (Y/N): _

---

## In `[reference].txt`

First, you have `[SCREEN TEXT]`. This is the text with ASS tags of the mask on the dialogue box of your video
resolution. The default is for resolution 1920 x 1440 (e.g. iPad Pro 2018 12.9-inch) and another reference
file (`[1920x1342]reference.txt`) stores the one for resolution 1920 x 1342 (e.g. iPad Pro 2020 11.0-inch). See *
Additional Help* section "creating a mask in Aegisub" for how to create a mask.

    [SCREEN TEXT]
    aegisub_tags_and_text

Next, you have `[LOCATION SCREEN TEXT]` (the mask) and `[LOCATION TEXT]` (the text one the mask). These are for the
mask, and the subtitle text of location transitions similar to `[SCREEN TEXT]`.

    [LOCATION SCREEN TEXT]
    aegisub_tags_and_text
 
    [LOCATION TEXT]
    aegisub_tags_and_text

Next, you have the coordinates for the code to function properly at a certain resolution. See *Additional Help*
section "`settings.txt` and `[reference].txt`" for details.

    [TEXT FIR MX]
    x1 x2 y1 y2
 
    [TEXT REF MX]
    x1 x2 y1 y2
 
    [TEXT BOX MX]
    x1 x2 y1 y2
 
    [TEXT BOX BORDER MX]
    x1 x2 y1 y2

---

# Additional Help

The following guilds are available in the `[executable].zip` file from
the [release page](https://github.com/Jeunette/ASS-automation-java/releases).

* For `settings.txt` and `[reference].txt`, see `"help\[LANG]\Settings and Reference\*.png"`.
* For creating a mask in Aegisub, see `"help\[LANG]\Creating Masks in Aegisub.pdf"`. [(WIP)]()
* For burning `.ass` and adding filters to video, see `"help\[LANG]\How to Use Avidemux.pdf"`.

If you have further questions feel free to contact me via Discord - Jeunette🍹#6870 (yes, there is an emoji XD) - or
join [this server](https://discord.gg/pHCj3tjYwF).

---

# **Source Code Related**

This section is only for source code.

## Required External Files

* [ffmpeg](https://ffmpeg.org/download.html)
* [ffprobe](https://ffmpeg.org/download.html)