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
    public static final double SCREEN_TIME_OFFSET = 0.100;

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
        String jitterDescription = handler.getDescription(TYPE_JITTER);
        int[] imageRef = new int[imageSections.size()];
        int refTemp = 1;
        for (int i = 1; i < snippetSections.size(); i++) {
            for (int j = refTemp; j < imageSections.size(); j++) {
                if (snippetSections.get(i).dialogueCount > 2 && Math.abs(j - i) <= Math.abs(imageSections.size() - snippetSections.size())
                        && snippetSections.get(i).dialogueCount == imageSections.get(j).dialogueCount) {
                    imageRef[j] = i;
                    refTemp = j + 1;
                    break;
                }
            }
        }
        for (int i = 1; i < imageRef.length; i++) {
            if (imageRef[i] == 0 && imageRef[i - 1] != 0 && imageRef[i - 1] + 1 < snippetSections.size()
                    && snippetSections.get(imageRef[i - 1] + 1).dialogueCount == imageSections.get(i).dialogueCount)
                imageRef[i] = imageRef[i - 1] + 1;
        }
        for (int i = imageRef.length - 2; i >= 0; i--) {
            if (imageRef[i] == 0 && imageRef[i + 1] != 0 && imageRef[i + 1] - 1 >= 0
                    && snippetSections.get(imageRef[i + 1] - 1).dialogueCount == imageSections.get(i).dialogueCount)
                imageRef[i] = imageRef[i + 1] - 1;
        }
        int sum = 0;
        for (int i = 1; i < imageSections.size(); i++) { sum += imageSections.get(i).dialogues.size(); }
        System.out.printf("ImageSystems section size: %02d - %03d | ", imageSections.size(), sum);
        for (int i = 1; i < imageRef.length; i++) {
            System.out.printf("[%02d] ", i);
        }
        System.out.println();
        sum = 0;
        for (int i = 1; i < snippetSections.size(); i++) { sum += snippetSections.get(i).dialogues.size(); }
        System.out.printf("Snippets sections size:    %02d - %03d | ", snippetSections.size(), sum);
        for (int i = 1; i < imageRef.length; i++) {
            System.out.printf("[%02d] ", imageRef[i]);
        }
        System.out.println();
        locationCount = 0;
        sections.add(new EventSection());
        if (imageSections.get(0).screen == snippetSections.get(0).screen) {
            for (int i = snippetSections.get(0).transitionCount - 1; i >= 0; i--) {
                sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationText, false, false));
                sections.getLast().transitionCount ++;
                sections.getLast().transitions.getFirst().setTime(imageSections.get(1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                        imageSections.get(1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationScreenText, false, false));
                sections.getLast().transitions.getFirst().setTime(imageSections.get(1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                        imageSections.get(1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                System.out.println("\033[1;96mTODO\033[0m: Location transition detected ("
                        + getTimestamp(imageSections.get(1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                        + getTimestamp(imageSections.get(1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                locationCount++;
            }
        } else {
            throw new RuntimeException("FATAL ERROR!");
        }
        for (int i = 1; i < imageSections.size(); i++) {
            if (imageSections.get(i).screen != null && (imageRef[i] == 0 || snippetSections.get(imageRef[i]).screen != null)) {
                if (imageRef[i] != 0) {
                    fillSections(sections, imageSections, snippetSections, i, imageRef[i], locationDescription, locationScreenText, locationText);
                } else {
                    int imageIndex = i;
                    int snippetStartIndex = 0;
                    int snippetEndIndex = -1;
                    if ((imageRef[i - 1] != 0 || i - 1 == 0) && imageRef[i - 1] != snippetSections.size() - 1) {
                        snippetStartIndex = imageRef[i - 1] + 1;
                        while (imageIndex + 1 < imageRef.length && imageRef[imageIndex + 1] == 0) { imageIndex++; }
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
                        if (tempImageSection.getFirst().dialogues.size() == tempSnippetSection.getFirst().dialogues.size()) {
                            System.out.println("\033[1;93mATTENTION\u001B[0m: Paired combination in \033[1;97msection " + i + "*\u001B[0m ("
                                    + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                            for (int j = i; j <= imageIndex; j++) {
                                imageSections.get(j).comment();
                                sections.add(imageSections.get(j));
                            }
                            sections.get(i).dialogues.addFirst(new Event(CAUTION, ATTENTION, ATTENTION_TEXT, false, true));
                            sections.get(i).dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(imageIndex).screen.endTime);
                            fillSections(sections, tempImageSection, tempSnippetSection, 0, 0, locationDescription, locationScreenText, locationText);
                        } else {
                            if (Math.abs(tempImageSection.getFirst().dialogues.size() - tempSnippetSection.getFirst().dialogues.size()) <= 1) {
                                System.out.println("\033[1;95mERROR\033[0m: Single line mismatched in \033[1;97msection " + i + "*\u001B[0m ("
                                        + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                                for (int j = i; j <= imageIndex; j++) {
                                    imageSections.get(j).comment();
                                    sections.add(imageSections.get(j));
                                }
                                sections.get(i).dialogues.addFirst(new Event(CAUTION, ERROR, ERROR_TEXT, false, true));
                                sections.get(i).dialogues.getFirst().setTime(imageSections.get(i).screen.startTime, imageSections.get(imageIndex).screen.endTime);
                                fillSections(sections, tempImageSection, tempSnippetSection, 0, 0, locationDescription, locationScreenText, locationText);
                            } else {
                                System.out.println("\033[1;91mCRITICAL_ERROR\033[0m: " + (tempImageSection.getFirst().dialogues.size() - tempSnippetSection.getFirst().dialogues.size())
                                        + " lines mismatched in \033[1;97msection " + i + "*\u001B[0m (" + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
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
                    }else if (imageSections.get(i).dialogues.size() != 0){
                        System.out.println("\033[1;91mCRITICAL_ERROR\033[0m: None matches for \033[1;97msection " + i + "\u001B[0m ("
                                + imageSections.get(i).screen.start + " - " + imageSections.get(i).screen.end + ").");
                        sections.add(imageSections.get(i));
                        sections.getLast().dialogues.addFirst(new Event(CAUTION, CRITICAL, CRITICAL_TEXT, false, true));
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
        for (int j = 0; j < snippetSections.get(i2).dialogues.size(); j++) {
            snippet = snippetSections.get(i2).dialogues.get(j);
            int diff = imageSections.get(i).dialogues.size() - snippetSections.get(i2).dialogues.size();
            if (j < diff) {
                sections.getLast().dialogues.add(new Event(snippet.style, snippet.name, ERROR + " -> " + snippet.text, snippet.fade, true));
                sections.getLast().dialogues.getLast().setTime(image.startTime, image.startTime);
            } else {
                image = imageSections.get(i).dialogues.get(Math.min(j + (Math.max(diff, 0)), imageSections.get(i).dialogues.size() - 1));
                sections.getLast().dialogues.add(new Event(snippet.style, snippet.name, snippet.text, snippet.fade, false));
                sections.getLast().dialogues.getLast().setTime(image.startTime, image.endTime);
            }
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
                sections.getLast().transitions.getFirst().setTime(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                        imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                sections.getLast().transitions.addFirst(new Event(Transition.TRANSITION_STYLE, TODO, locationScreenText, false, false));
                sections.getLast().transitions.getFirst().setTime(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount,
                        imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1));
                System.out.println("\033[1;96mTODO\033[0m: Location transition detected ("
                        + getTimestamp(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * sections.getLast().transitionCount) + " - "
                        + getTimestamp(imageSections.get(i + 1).dialogues.getFirst().startTime - TIME_LOCATION_SEC * (sections.getLast().transitionCount - 1)) + ").");
                locationCount++;
            }
        }
        if (transitions.transitions.size() != snippetSections.get(i2).transitionCount) {
            System.out.println("\033[1;95mDISTORTION\033[0m: Distortion transition(s) / effect(s) found in \033[1;97msection " + ((i != 0) ? "" + i : "combined") + "\u001B[0m ("
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
            System.out.println("Section: " + i + " Size :" + sections.get(i).dialogues.size());
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
            System.out.println("Section: " + i + " Size :" + sections.get(i).dialogues.size());
        }
    }

    public static LinkedList<EventSection> getEventSections(ImageSystem system, File ref) throws FileNotFoundException {
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
                        if (sections.getLast().dialogues.size() != 0) {
                            sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1)); }
                        else { sections.getLast().screen.setTime(timestampIn, timestampIn); }
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
                        if (sections.getLast().dialogues.size() != 0) {
                            sections.getLast().screen.setTime(timestampIn, timestamps.get(results.get(i + 1).index + 1)); }
                        else { sections.getLast().screen.setTime(timestampIn, timestampIn); }
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

    public static void write(ImageSystem system, Snippets snippets, File ref, File ass) throws IOException {
        File defaultASS = new File(DEFAULT_ASS);
        Files.copy(defaultASS.toPath(), ass.toPath(), StandardCopyOption.REPLACE_EXISTING);
        LinkedList<EventSection> imageSections = getEventSections(system, ref);
        LinkedList<EventSection> snippetSections = snippets.getEventSections();
        LinkedList<EventSection> sections = validateEventSections(imageSections, snippetSections);
        if (locationCount != snippets.locationCount) {
            if (locationCount < snippets.locationCount) {
                System.out.println("\033[1;91mCRITICAL_ERROR\033[0m: Location Transitiom amount mismatched.");
                sections.getFirst().screen = new Event(CAUTION, CRITICAL, "{\\an8\\fs80\\bord8}Please inspect & locate more location transitions.", false, false);
            } else {
                System.out.println("\033[1;30m\033[0;101mFATAL_ERROR\033[0m: Location Transitiom amount \033[1;91mEXCEEDED\033[0m.");
                sections.getFirst().screen = new Event(CAUTION, FATAL, "FATAL ERROR\\N{\\fs80\\bord8} Please inspect & modify extra location transitions.\\N{\\fs300\\bord30}:P", false, false);
            }
            sections.getFirst().screen.setTime(0, imageSections.getLast().dialogues.getLast().endTime);
            System.out.println("Location transition discovered:    " + locationCount);
            System.out.println("Actual location transition amount: " + snippets.locationCount);
        } else {
            System.out.println("\033[1;93mATTENTION\u001B[0m\033[1;97m - \033[1;96mTODO\033[0m: \033[1;97m" + locationCount + "\033[0m location transition detected.");
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
        LinkedList<StringBuilder> events = new LinkedList<>();
        events.add(new StringBuilder());
        events.add(new StringBuilder());
        events.add(new StringBuilder());
        for (EventSection section : sections) {
            if (section.screen != null && section.screen.endTime - section.screen.startTime != 0 ) {
                section.screen.setTime(section.screen.startTime - SCREEN_TIME_OFFSET, section.screen.endTime + SCREEN_TIME_OFFSET);
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
