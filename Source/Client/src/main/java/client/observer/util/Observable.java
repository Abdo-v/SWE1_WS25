package client.observer.util;

import client.model.GameStateEventType;

public interface Observable {

   /**
     * Adds an observer to this observable
     * @param observer The observer to add
     */
    void addObserver(Observer observer);

    /**
     * Adds an observer only for a specific event type.
     *
     * @param eventType The event type the observer is interested in
     * @param observer  The observer to add
     */
    void addObserver(GameStateEventType eventType, Observer observer);
    
    /**
     * Removes an observer from this observable
     * @param observer The observer to remove
     */
    void removeObserver(Observer observer);

    /**
     * Removes an observer from a specific event type subscription.
     *
     * @param eventType The event type subscription to remove from
     * @param observer  The observer to remove
     */
    void removeObserver(GameStateEventType eventType, Observer observer);
    
    /**
     * Notifies all observers registered for all events.
     */
    void notifyObservers();

    /**
     * Notifies observers interested in a specific event type.
     *
     * @param eventType The type of change that occurred
     */
    void notifyObservers(GameStateEventType eventType);

}
