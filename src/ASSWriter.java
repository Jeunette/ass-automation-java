import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class ASSWriter {

    public static final String DEFAULT_ASS = "untitled.ass";
    public static final String REF_DELIMITER = ",";
    public static final String TIMESTAMP_FORMAT = "%d:%02d:%02d.%02d";

    public static final String TODO = "TODO";

    public static final String FATAL = "FATAL";
    public static final String CRITICAL = "CRITICAL";
    public static final String ERROR = "ERROR";
    public static final String DISTORTION = "DISTORTION";
    public static final String ATTENTION = "ATTENTION";
    public static final String IGNORE = "IGNORE";
    public static final String CAUTION = "CAUTION";
    public static final String DELETE = "DELETE";
    public static final String CRITICAL_TEXT = "CRITICAL_ERROR!\\N{\\fs120\\bord12}INSPECTION & AMENDMENT REQUIRED.";
    public static final String ERROR_TEXT = "ERROR!\\N{\\fs120\\bord12}INSPECTION & AMENDMENT REQUIRED.";
    public static final String DISTORTION_TEXT = "DISTORTION_DETECTED!\\N{\\fs120\\bord12}INSPECTION & AMENDMENT REQUIRED.";
    public static final String ATTENTION_TEXT = "ATTENTION!\\N{\\fs120\\bord12}INSPECTION REQUIRED.";
    public static final String IGNORE_TEXT = "DELETE OR COMMENT.\\N{\\fs120\\bord12}USE FOR REFERENCE ONLY.";

    public static final double MIN_TIME = 0.46;
    public static final double SCREEN_START_OFFSET_DEFAULT = -0.100;
    public static final double SCREEN_END_OFFSET_DEFAULT = 0.000;

    public static final int TYPE_JITTER = 6;
    public static final int TYPE_LOCATION = 8;
    public static final int TIME_LOCATION_SEC = 3;

    public static int locationCount;

    public static LinkedList<EventSection> validateEventSections(LinkedList<EventSection> imageSections, LinkedList<EventSection> snippetSections) throws IOException {
        LinkedList<EventSection> sections = new LinkedList<>();
        String locationScreenText = SettingsHandler.refReader(SettingsHandler.REF_CAT_LOCATION_SCREEN_TEXT);
        String locationText = SettingsHandler.refReader(SettingsHandler.REF_CAT_LOCATION_TEXT);
        TransitionHandler handler = new TransitionHandler();
        String locationDescription = handler.getDescription(TYPE_LOCATION);
        // String jitterDescription = handler.getDescription(TYPE_JITTER);
        int[] imageRef = new int[imageSections.size()];
        int[] snippetRef = new int[snippetSections.size()];
        int refTemp = 1;
        for (int i = 1; i < snippetSections.size(); i++) {
            for (int j = refTemp; j < imageSections.size(); j++) {
                if (snippetSections.get(i).dialogueCount > 2 && Math.abs(j - i) <= Math.abs(imageSections.size() - snippetSections.size())
                        && snippetSections.get(i).dialogueCount == imageSections.get(j).dialogueCount) {
                    imageRef[j] = i;
                    snippetRef[i] = j;
                    refTemp = j + 1;
                    break;
                }
            }
        }
        for (int i = 1; i < imageRef.length; i++) {
            if (imageRef[i] == 0 && imageRef[i - 1] != 0 && imageRef[i - 1] + 1 < snippetSections.size() && snippetRef[imageRef[i - 1] + 1] == 0
                    && snippetSections.get(imageRef[i - 1] + 1).dialogueCount == imageSections.get(i).dialogueCount) {
                imageRef[i] = imageRef[i - 1] + 1;
                snippetRef[imageRef[i]] = i;
            }
        }
        for (int i = imageRef.length - 2; i >= 0; i--) {
            if (imageRef[i] == 0 && imageRef[i + 1] != 0 && imageRef[i + 1] - 1 >= 0 && snippetRef[imageRef[i + 1] - 1] == 0
                    && snippetSections.get(imageRef[i + 1] - 1).dialogueCount == imageSections.get(i).dialogueCount) {
                imageRef[i] = imageRef[i + 1] - 1;
                snippetRef[imageRef[i]] = i;
            }
        }
        int sum = 0;
        for (int i = 1; i < imageSections.size(); i++) {
            sum += imageSections.get(i).dialogueCount;
        }
        int imageSectionsRealSize = 1;
        for (EventSection section : imageSections) {
            if (section.dialogueCount != 0) imageSectionsRealSize++;
        }
        System.out.printf("[OPCV] %02d%s - %03d | ", imageSectionsRealSize, (imageSections.size() == imageSectionsRealSize) ? "" : "*", sum);
        for (int i = 1; i < imageRef.length; i++) {
            if (imageSections.get(i).dialogueCount != 0) System.out.printf("[%02d] ", i);
        }
        System.out.println();
        sum = 0;
        for (int i = 1; i < snippetSections.size(); i++) {
            sum += snippetSections.get(i).dialogueCount;
        }
        System.out.printf("[JSON] %02d%s - %03d | ", snippetSections.size(), (imageSections.size() == imageSectionsRealSize) ? "" : " ", sum);
        for (int i = 1; i < imageRef.length; i++) {
            if (imageSections.get(i).dialogueCount != 0) System.out.printf("[%02d] ", imageRef[i]);
        }
        System.out.println();
        locationCount = 0;
        int offset = 0;
        for (int i = 1; i < imageSections.size(); i++) {
            if (imageSections.get(i).dialogueCount == 0) {
                offset++;
            } else {
                break;
            }
        }
        sections.add(new EventSection());
        if (imageSections.get(0).screen == snippetSections.get(0).screen) {
            for (int i = snippetSections.get(0).transitionCount - 1; i >= 0; i--) {
                sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationText, false, false));
                sections.getLast().transitionCount++;
                sections.getLast().transitions.getFirst().setTime(imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                        imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationScreenText, false, false));
                sections.getLast().transitions.getFirst().setTime(imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                        imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                System.out.println("[TODO] Location transition detected ("
                        + getTimestamp(imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                        + getTimestamp(imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                Logger.out.println("[TODO] Location transition detected ("
                        + getTimestamp(imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                        + getTimestamp(imageSections.get(offset + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                locationCount++;
            }
        } else {
            throw new RuntimeException("FATAL ERROR!");
        }
        for (int i = offset + 1; i < imageSections.size(); i++) {
            if (imageSections.get(i).screen != null && (imageRef[i] == 0 || snippetSections.get(imageRef[i]).screen != null)) {
                if (imageRef[i] != 0) {
                    fillSections(sections, imageSections, snippetSections, i, imageRef[i], locationDescription, locationScreenText, locationText);
                } else {
                    int imageIndex = i;
                    int snippetStartIndex = 0;
                    int snippetEndIndex = -1;
                    if ((imageRef[i - 1] != 0 || i - 1 == 0) && imageRef[i - 1] != snippetSections.size() - 1) {
                        snippetStartIndex = imageRef[i - 1] + 1;
                        while (imageIndex + 1 < imageRef.length && imageRef[imageIndex + 1] == 0) {
                            imageIndex++;
                        }
                        snippetEndIndex = (imageIndex + 1 < imageRef.length) ? imageRef[imageIndex + 1] - 1 : snippetSections.size() - 1;
                    }
                    if ((imageRef[i - 1] != 0 || i - 1 == 0) && snippetEndIndex - snippetStartIndex >= 0) {
                        LinkedList<EventSection> tempImageSection = new LinkedList<>();
                        LinkedList<EventSection> tempSnippetSection = new LinkedList<>();
                        tempImageSection.add(new EventSection());
                        tempSnippetSection.add(new EventSection());
                        System.out.print("Matching ImageSystem section(s) [");
                        for (int j = i; j <= imageIndex; j++) {
                            tempImageSection.getFirst().append(imageSections.get(j));
                            System.out.printf("%02d%s ", j , (j < imageIndex) ? "," : "]");
                        }
                        System.out.print("with Snippets section(s) [");
                        for (int j = snippetStartIndex; j <= snippetEndIndex; j++) {
                            tempSnippetSection.getFirst().append(snippetSections.get(j));
                            System.out.printf("%02d%s ", j , (j < snippetEndIndex) ? "," : "]");
                        }
                        System.out.println();
                        tempImageSection.getFirst().screen.style = DELETE;
                        tempImageSection.getFirst().screen.name = IGNORE;
                        tempImageSection.getFirst().screen.text = IGNORE_TEXT;
                        if (imageIndex + 1 < imageSections.size())
                            tempImageSection.add(imageSections.get(imageIndex + 1));
                        if (tempImageSection.getFirst().dialogueCount == tempSnippetSection.getFirst().dialogueCount) {
                            System.out.println("[ATTENTION] Paired combination in section " + i + "* ("
                                    + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                            Logger.out.println("[ATTENTION] Paired combination in section " + i + "* ("
                                    + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                            int tempIndex = sections.size() - 1;
                            for (int j = i; j <= imageIndex; j++) {
                                imageSections.get(j).comment();
                                sections.add(imageSections.get(j));
                            }
                            sections.get(tempIndex).dialogues.addFirst(new Event(CAUTION, ATTENTION, ATTENTION_TEXT, false, true));
                            sections.get(tempIndex).dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(imageIndex).screen.endTime);
                            fillSections(sections, tempImageSection, tempSnippetSection, 0, 0, locationDescription, locationScreenText, locationText);
                        } else {
                            if (Math.abs(tempImageSection.getFirst().dialogueCount - tempSnippetSection.getFirst().dialogueCount) <= 1) {
                                System.out.println("[ERROR] Single line mismatched in section " + i + "* ("
                                        + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                                Logger.out.println("[ERROR] Single line mismatched in section " + i + "* ("
                                        + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                                for (int j = i; j <= imageIndex; j++) {
                                    imageSections.get(j).comment();
                                    sections.add(imageSections.get(j));
                                }
                                int tempIndex = sections.size() - 1;
                                sections.get(tempIndex).dialogues.addFirst(new Event(CAUTION, ERROR, ERROR_TEXT, false, true));
                                sections.get(tempIndex).dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(imageIndex).screen.endTime);
                                fillSections(sections, tempImageSection, tempSnippetSection, 0, 0, locationDescription, locationScreenText, locationText);
                            } else {
                                System.out.println("[CRITICAL_ERROR] " + (tempImageSection.getFirst().dialogueCount - tempSnippetSection.getFirst().dialogueCount)
                                        + " lines mismatched in section " + i + "* (" + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                                Logger.out.println("[CRITICAL_ERROR] " + (tempImageSection.getFirst().dialogueCount - tempSnippetSection.getFirst().dialogueCount)
                                        + " lines mismatched in section " + i + "* (" + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                                fillSections(sections, tempImageSection, tempSnippetSection, 0, 0, locationDescription, locationScreenText, locationText);
                                sections.getLast().comment();
                                sections.getLast().dialogues.addFirst(new Event(CAUTION, CRITICAL, CRITICAL_TEXT, false, true));
                                sections.getLast().dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(imageIndex).screen.endTime);
                                for (int j = i; j <= imageIndex; j++) {
                                    sections.add(imageSections.get(j));
                                }
                            }
                        }
                        i += imageIndex - i;
                    } else if (imageSections.get(i).dialogueCount != 0) {
                        System.out.println("[CRITICAL_ERROR] None matches for section " + i + " ("
                                + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                        Logger.out.println("[CRITICAL_ERROR] None matches for section " + i + " ("
                                + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                        sections.add(imageSections.get(i));
                        sections.getLast().dialogues.addFirst(new Event(CAUTION, CRITICAL, CRITICAL_TEXT, false, true));
                        sections.getLast().dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(i).screen.endTime);
                    } else if (imageSections.get(i).dialogueCount == 0 && (i == 1 || imageSections.get(i - 1).dialogueCount != 0)) {
                        System.out.println("[ERROR] Shift (empty section) found in section " + i + " ("
                                + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                        Logger.out.println("[ERROR] Shift (empty section) found in section " + i + " ("
                                + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                        sections.add(imageSections.get(i));
                        sections.getLast().dialogues.addFirst(new Event(CAUTION, ERROR, ERROR_TEXT, false, true));
                        sections.getLast().dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(i).screen.endTime);
                    }
                }
            } else {
                    throw new RuntimeException("FATAL ERROR!");
            }
        }
        return sections;
    }

    private static void fillSections(LinkedList<EventSection> sections, LinkedList<EventSection> imageSections, LinkedList<EventSection> snippetSections,
                                     int i, int i2, String locationDescription, String locationScreenText, String locationText) {
        sections.add(new EventSection());
        Event snippet;
        Event image = imageSections.get(i).screen;
        sections.getLast().screen = new Event(image.style, image.name, image.text, false, true);
        sections.getLast().screen.setTime(image.startTime, image.endTime);
        int diff = imageSections.get(i).dialogueCount - snippetSections.get(i2).dialogueCount;
        for (int j = 0; j < diff; j++) {
            image = imageSections.get(i).dialogues.get(j);
            sections.getLast().dialogues.add(new Event(CAUTION, ERROR, null, false, false));
            sections.getLast().dialogues.getLast().setTime(image.startTime, image.endTime);
            sections.getLast().dialogueCount++;
        }
        for (int j = 0; j < snippetSections.get(i2).dialogueCount; j++) {
            snippet = snippetSections.get(i2).dialogues.get(j);
            if (j + diff < 0) {
                sections.getLast().dialogues.add(new Event(snippet.style, snippet.name, ERROR + " -> " + snippet.text, snippet.fade, false));
                sections.getLast().dialogues.getLast().setTime(image.startTime, image.startTime);
            } else {
                image = imageSections.get(i).dialogues.get(j + diff);
                sections.getLast().dialogues.add(new Event(snippet.style, snippet.name, snippet.text, snippet.fade, false));
                sections.getLast().dialogues.getLast().setTime(image.startTime, image.endTime);
            }
            sections.getLast().dialogueCount++;
        }
        EventSection transitions = new EventSection();
        for (int j = 0; j < snippetSections.get(i2).transitionCount; j ++) {
            if (snippetSections.get(i2).transitions.get(j).text.contains(locationDescription)) {
                transitions.transitions.add(snippetSections.get(i2).transitions.get(j));
                transitions.transitionCount++;
            }
        }
        for (int j = transitions.transitionCount - 1; j >= 0; j--) {
            if (i + 1 < imageSections.size()) {
                sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationText, false, false));
                sections.getLast().transitionCount++;
                if (imageSections.get(i + 1).dialogues.size() != 0 && sections.getLast().transitions != null) {
                    sections.getLast().transitions.getFirst().setTime(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                            imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                    sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationScreenText, false, false));
                    sections.getLast().transitions.getFirst().setTime(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                            imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                    System.out.println("[TODO] Location transition detected ("
                            + getTimestamp(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                            + getTimestamp(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                    Logger.out.println("[TODO] Location transition detected ("
                            + getTimestamp(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                            + getTimestamp(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                } else {
                    sections.getLast().transitions.getFirst().setTime(imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                            imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                    sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationScreenText, false, false));
                    sections.getLast().transitions.getFirst().setTime(imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                            imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                    System.out.println("[TODO] Location transition detected ("
                            + getTimestamp(imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                            + getTimestamp(imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                    Logger.out.println("[TODO] Location transition detected ("
                            + getTimestamp(imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                            + getTimestamp(imageSections.get(i).dialogues.getLast().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                }
                locationCount++;
            }
        }
        if (transitions.transitionCount != snippetSections.get(i2).transitionCount) {
            System.out.println("[DISTORTION] Distortion transition(s) / effect(s) found in section " + ((i != 0) ? "" + i : "combined") + " ("
                    + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
            Logger.out.println("[DISTORTION] Distortion transition(s) / effect(s) found in section " + ((i != 0) ? "" + i : "combined") + " ("
                    + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
            sections.getLast().dialogues.addFirst(new Event(CAUTION, DISTORTION, DISTORTION_TEXT, false, false));
            sections.getLast().dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(i).screen.endTime);
            sections.getLast().dialogues.addFirst(new Event(CAUTION, ERROR, DISTORTION_TEXT, false, true));
            sections.getLast().dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(i).screen.endTime);
        }
    }

    public static void printSections(LinkedList<EventSection> sections) {
        System.out.println(sections.toString());
        for (int i = 0; i < sections.size(); i++) {
            System.out.println("Section: " + i + " Size :" + sections.get(i).dialogueCount);
            System.out.println(sections.get(i).screen);
            for (int j = 0; j < sections.get(i).dialogueCount; j++) {
                System.out.println(sections.get(i).dialogues.get(j));
            }
            for (int j = 0; j < sections.get(i).transitionCount; j++) {
                System.out.println(sections.get(i).transitions.get(j));
            }
        }
    }

    public static void printSectionsSimple(LinkedList<EventSection> sections) {
        System.out.println(sections.toString());
        for (int i = 0; i < sections.size(); i++) {
            System.out.println("Section: " + i + " Size :" + sections.get(i).dialogueCount);
        }
    }

    public static LinkedList<EventSection> getEventSectionsOpenCV(ImageSystem system, String path) throws IOException {
        LinkedList<ImageDataResult> results = system.getFormattedResults();
        TimestampOpenCV timestamps = new TimestampOpenCV(path);
        LinkedList<EventSection> sections = new LinkedList<>();
        sections.add(new EventSection());
        double timestampIn = 0.0;
        String screenText = SettingsHandler.refReader(SettingsHandler.REF_CAT_SCREEN_TEXT);
        sections.add(new EventSection(screenText));
        for (int i = 0; i < results.size() - 1; i++) {
            if (results.get(i).start) {
                if (results.get(i).in) timestampIn = timestamps.get(results.get(i).index);
                if (results.get(i + 1).out) {
                    if (timestamps.get(results.get(i + 1).index) - timestamps.get(results.get(i).index) < MIN_TIME) {
                        if (sections.getLast().dialogueCount != 0) {
                            sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1));
                        } else {
                            sections.getLast().screen.setTime(timestampIn, timestampIn);
                        }
                        sections.add(new EventSection(screenText));
                        if (i + 1 < results.size()) {
                            timestampIn = timestamps.get(results.get(i + 1).index);
                        }
                        continue;
                    }
                    sections.getLast().dialogues.add(new Event(CAUTION, ATTENTION, null, true));
                    sections.getLast().dialogues.getLast().setTime(timestamps.get(results.get(i).index), timestamps.get(results.get(i + 1).index + 1));
                    sections.getLast().dialogueCount++;
                    sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1));
                    sections.add(new EventSection(screenText));
                } else {
                    if (timestamps.get(results.get(i + 1).index) - timestamps.get(results.get(i).index) < MIN_TIME) {
                        if (sections.getLast().dialogueCount != 0) {
                            sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1));
                        } else {
                            sections.getLast().screen.setTime(timestampIn, timestampIn);
                        }
                        sections.add(new EventSection(screenText));
                        if (i + 1 < results.size()) {
                            timestampIn = timestamps.get(results.get(i + 1).index);
                        }
                        continue;
                    }
                    sections.getLast().dialogues.add(new Event(CAUTION, ATTENTION, null, false));
                    sections.getLast().dialogues.getLast().setTime(timestamps.get(results.get(i).index), timestamps.get(results.get(i + 1).index));
                    sections.getLast().dialogueCount++;
                }
            }
        }
        sections.removeLast();
        return sections;
    }

    public static LinkedList<EventSection> getEventSections(ImageSystem system, File ref) throws IOException {
        LinkedList<ImageDataResult> results = system.getFormattedResults();
        ArrayList<Double> timestamps = getTimeStampReference(ref);
        LinkedList<EventSection> sections = new LinkedList<>();
        sections.add(new EventSection());
        double timestampIn = 0.0;
        String screenText = SettingsHandler.refReader(SettingsHandler.REF_CAT_SCREEN_TEXT);
        sections.add(new EventSection(screenText));
        for (int i = 0; i < results.size() - 1; i++) {
            if (results.get(i).start) {
                if (results.get(i).in) timestampIn = timestamps.get(results.get(i).index);
                if (results.get(i + 1).out) {
                    if (timestamps.get(results.get(i + 1).index) - timestamps.get(results.get(i).index) < MIN_TIME) {
                        if (sections.getLast().dialogueCount != 0) {
                            sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1));
                        } else {
                            sections.getLast().screen.setTime(timestampIn, timestampIn);
                        }
                        sections.add(new EventSection(screenText));
                        if (i + 1 < results.size()) { timestampIn = timestamps.get(results.get(i + 1).index); }
                        continue;
                    }
                    sections.getLast().dialogues.add(new Event(CAUTION, ATTENTION, null, true));
                    sections.getLast().dialogues.getLast().setTime(timestamps.get(results.get(i).index), timestamps.get(results.get(i + 1).index + 1));
                    sections.getLast().dialogueCount++;
                    sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1));
                    sections.add(new EventSection(screenText));
                } else {
                    if (timestamps.get(results.get(i + 1).index) - timestamps.get(results.get(i).index) < MIN_TIME) {
                        if (sections.getLast().dialogueCount != 0) {
                            sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1));
                        } else {
                            sections.getLast().screen.setTime(timestampIn, timestampIn);
                        }
                        sections.add(new EventSection(screenText));
                        if (i + 1 < results.size()) { timestampIn = timestamps.get(results.get(i + 1).index); }
                        continue;
                    }
                    sections.getLast().dialogues.add(new Event(CAUTION, ATTENTION, null, false));
                    sections.getLast().dialogues.getLast().setTime(timestamps.get(results.get(i).index), timestamps.get(results.get(i + 1).index));
                    sections.getLast().dialogueCount++;
                }
            }
        }
        sections.removeLast();
        return sections;
    }

    public static void writeOpenCV(ImageSystem system, Snippets snippets, File ass, String path) throws IOException {
        LinkedList<EventSection> imageSections = getEventSectionsOpenCV(system, path);
        write(snippets, ass, imageSections);
    }

    public static void writeFfprobe(ImageSystem system, Snippets snippets, File ref, File ass) throws IOException {
        LinkedList<EventSection> imageSections = getEventSections(system, ref);
        write(snippets, ass, imageSections);
    }

    public static void write(Snippets snippets, File ass, LinkedList<EventSection> imageSections) throws IOException {
        File defaultASS;
        try {
            defaultASS = new File(SettingsHandler.reader(SettingsHandler.CAT_ASS_PATH));
        } catch (Exception ignored) {
            defaultASS = new File(DEFAULT_ASS);
        }
        Files.copy(defaultASS.toPath(), ass.toPath(), StandardCopyOption.REPLACE_EXISTING);
        LinkedList<EventSection> snippetSections = snippets.getEventSections();
        LinkedList<EventSection> sections = validateEventSections(imageSections, snippetSections);
        if (locationCount != snippets.locationCount) {
            if (locationCount < snippets.locationCount) {
                System.out.println("[CRITICAL_ERROR] Location Transition amount mismatched.");
                Logger.out.println("[CRITICAL_ERROR] Location Transition amount mismatched.");
                sections.getFirst().screen = new Event(CAUTION, CRITICAL, "{\\an8\\fs80\\bord8}Please inspect & locate more location transitions.", false, false);
            } else {
                System.out.println("[FATAL_ERROR] Location Transition amount EXCEEDED.");
                Logger.out.println("[FATAL_ERROR] Location Transition amount EXCEEDED.");
                sections.getFirst().screen = new Event(CAUTION, FATAL, "FATAL ERROR\\N{\\fs80\\bord8} Please inspect & modify extra location transitions.\\N{\\fs300\\bord30}:P", false, false);
            }
            sections.getFirst().screen.setTime(0, imageSections.getLast().dialogues.getLast().endTime);
            System.out.println("Location transition discovered:    " + locationCount);
            Logger.out.println("Location transition discovered:    " + locationCount);
            System.out.println("Actual location transition amount: " + snippets.locationCount);
            Logger.out.println("Actual location transition amount: " + snippets.locationCount);
        } else {
            System.out.println("[ATTENTION] - [TODO] " + locationCount + " location transition detected.");
            Logger.out.println("[ATTENTION] - [TODO] " + locationCount + " location transition detected.");
        }
        LinkedList<StringBuilder> events = getEvents(sections);
        FileWriter temp = new FileWriter(ass, true);
        BufferedWriter writer = new BufferedWriter(temp);
        PrintWriter printer = new PrintWriter(writer);
        printer.print(events.get(0));
        printer.print(events.get(1));
        printer.print(events.get(2));
        printer.close();
        writer.close();
        temp.close();
    }

    public static LinkedList<StringBuilder> getEvents(LinkedList<EventSection> sections) {
        double startOffset = SCREEN_START_OFFSET_DEFAULT;
        double endOffset = SCREEN_END_OFFSET_DEFAULT;
        try {
            startOffset = Double.parseDouble(SettingsHandler.reader(SettingsHandler.CAT_SCREEN_START_OFFSET));
        } catch (Exception ignored) {
        }
        try {
            endOffset = Double.parseDouble(SettingsHandler.reader(SettingsHandler.CAT_SCREEN_END_OFFSET));
        } catch (Exception ignored) {
        }
        LinkedList<StringBuilder> events = new LinkedList<>();
        events.add(new StringBuilder());
        events.add(new StringBuilder());
        events.add(new StringBuilder());
        for (EventSection section : sections) {
            if (section.screen != null && section.screen.endTime - section.screen.startTime != 0) {
                section.screen.setTime(section.screen.startTime + startOffset, section.screen.endTime + endOffset);
                events.get(0).append(section.screen.toString()).append("\n");
            }
            for (Event event : section.transitions) {
                events.get(1).append(event.toString()).append("\n");
            }
            for (Event event : section.dialogues) {
                events.get(2).append(event.toString()).append("\n");
            }
        }
        return events;
    }

    public static ArrayList<Double> getTimeStampReference (File ref) throws FileNotFoundException {
        ArrayList<Double> timestamps = new ArrayList<>();
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(ref)));
        while (scanner.hasNextLine()) {
            String temp = scanner.nextLine();
            if (temp.contains(REF_DELIMITER)) {
                timestamps.add(Double.parseDouble(temp.substring(0, temp.indexOf(REF_DELIMITER))));
            }
        }
        scanner.close();
        return timestamps;
    }

    public static String getTimestamp (double info) {
        int sec = (int) Math.floor(info);
        info -= sec;
        return String.format(TIMESTAMP_FORMAT, sec / 60 / 60, sec /60 % 60, sec % 60 + Math.round(info * 1000) / 1000, Math.round(info * 1000) / 10 % 100);
    }

}
