import io.reactivex.Flowable;
import io.reactivex.Observable;

public class ActiveProgramming {
    public static void main(String[] args){
        Flowable.just("Hello world").subscribe(System.out::println);
        
    }
}
