import java.util.ArrayList;

public class StockGrabber implements Subject {

    private ArrayList<Observer> observers;
    private double ibmPrice;
    private double applePrice;
    private double googlePrice;

    public StockGrabber() {
        observers = new ArrayList<>();
    }

    @Override
    public void register(Observer o) {
        observers.add(o);
    }

    @Override
    public void unregister(Observer o) {
        int idx = observers.indexOf(o); // index of removeObserver
        System.out.print("Observer "+(idx+1)+" deleted");
        observers.remove(idx);
    }

    @Override
    public void notifiObserver() {
        for(Observer observer : observers){
            observer.update(ibmPrice, applePrice,googlePrice);
        }
    }

    public void setIbmPrice(double ibmPrice){
        this.ibmPrice = ibmPrice;
        notifiObserver();
    }

    public void setApplePrice(double applePrice){
        this.applePrice = applePrice;
        notifiObserver();
    }
    public void setGooglePrice(double googlePrice){
        this.googlePrice = googlePrice;
        notifiObserver();
    }
}
