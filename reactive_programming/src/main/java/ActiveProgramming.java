import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ActiveProgramming {
    public static void main(String[] args){
        Observable<Integer> source = Observable.range(1,5);

        Observer<Integer> consumer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println(d.toString());
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("innit "+integer);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        };

        source.subscribe(consumer);

    }
}
