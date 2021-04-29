public interface ActionContent {

    int TYPE_DIALOGUE = 1;
    int TYPE_TRANSITION = 6;

    String DIALOGUE = "Dialogue";
    String TRANSITION = "Transition";

    String CONTENT_SPlITER = " = ";
    String TEXT_SPlITER = ": ";
    String DATA_SPlITER = ", ";

    String DIALOGUE_STYLE = "Default";
    String TRANSITION_STYLE = "screen";
    String TRANSITION_NAME = "screen";

    Event toEvent();

}
