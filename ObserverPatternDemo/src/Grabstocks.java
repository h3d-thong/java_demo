public class Grabstocks {
    public static void main(String[] args){
        StockGrabber stockGrabber = new StockGrabber();

        StockObserver stockObserver1 = new StockObserver(stockGrabber);
        stockGrabber.setIbmPrice(123.21);
        stockGrabber.setApplePrice(321.123);
        stockGrabber.setGooglePrice(671.28);

        StockObserver stockObserver2 = new StockObserver(stockGrabber);
        stockGrabber.setIbmPrice(123.21);
        stockGrabber.setApplePrice(321.123);
        stockGrabber.setGooglePrice(671.28);

    }
}
