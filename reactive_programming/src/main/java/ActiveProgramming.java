import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActiveProgramming {
    public static void main(String[] args){
        //một luồng cơ bản, do chưa có API nên dùng một số kiểu nguyên thửy
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


        //flatMap thực hiện tiến trình song song và cho ra kết quả không theo thứ tự
        Flowable.range(1, 10)
                .flatMap(v ->
                        Flowable.just(v)
                                .subscribeOn(Schedulers.computation())
                                .map(w -> w * w)
                )
                .blockingSubscribe(System.out::println);

    }
}
