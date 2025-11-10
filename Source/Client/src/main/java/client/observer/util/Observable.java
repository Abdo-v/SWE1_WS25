package client.observer.util;

public interface Observable {

   /**
     * Adds an observer to this observable
     * @param observer The observer to add
     */
    void addObserver(Observer observer);
    
    /**
     * Removes an observer from this observable
     * @param observer The observer to remove
     */
    void removeObserver(Observer observer);
    
    /**
     * Notifies all registered observers about a state change
     * @param event The event describing the change
     */
    void notifyObservers();

}
