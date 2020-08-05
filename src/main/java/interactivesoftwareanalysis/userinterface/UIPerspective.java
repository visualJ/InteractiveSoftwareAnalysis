package interactivesoftwareanalysis.userinterface;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by benedikt.ringlein on 23.08.2016.
 */
@ToString @EqualsAndHashCode public class UIPerspective {

    private ListProperty<UIView> uiViews;
    private StringProperty name;
    @Getter @Setter private boolean pristine = true;

    public UIPerspective(String name){
        this.uiViews = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.name = new SimpleStringProperty(name);
    }

    public UIPerspective() {
        this("");
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ObservableList<UIView> getUiViews() {
        return uiViews.get();
    }

    public ListProperty<UIView> uiViewsProperty() {
        return uiViews;
    }

    public void addUIView(UIView uiView) {
        uiView.setUiPerspective(this);
        uiViews.add(uiView);
        new Thread(uiView::update).start();
    }

    public void removeUIView(UIView uiView) {
        uiViews.remove(uiView);
    }

    public void swapViewLeft(UIView uiView) {
        int index = uiViews.indexOf(uiView);
        if (index >= 1){
            UIView otherView = uiViews.get(index - 1);
            uiViews.remove(index);
            uiViews.remove(index - 1);
            uiViews.add(index - 1, uiView);
            uiViews.add(index, otherView);
        }
    }

    public void swapViewRight(UIView uiView) {
        int index = uiViews.indexOf(uiView);
        if (index < uiViews.size() - 1){
            UIView otherView = uiViews.get(index + 1);
            uiViews.remove(index + 1);
            uiViews.remove(index);
            uiViews.add(index, otherView);
            uiViews.add(index + 1, uiView);
        }
    }

    public void swapView(UIView oldView, UIView newView) {
        int index = uiViews.indexOf(oldView);
        uiViews.remove(index);
        newView.setUiPerspective(this);
        uiViews.add(index, newView);
        new Thread(newView::update).start();
    }
}
