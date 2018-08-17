import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class ActiveProgramming {
    public static void main(String[] args){
        
        User user1 = new User("Thong",23,0);
        User user2 = new User("Nguyen Van A",21,0);
        User user3 = new User("Tran Van B",28,21000);

        Observable<User> source = Observable.just(user1,user2,user3);

        Observer<User> consumer = new Observer<User>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println(d.toString());
            }

            @Override
            public void onNext(User user) {
                System.out.println("innit "+user.toString());
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
        user1.setName("thongvx");
        user1.setAge(25);
        consumer.onNext(user1);

        //flatMap thực hiện tiến trình song song và cho ra kết quả không theo thứ tự
        /*Flowable.range(1, 10)
                .flatMap(v ->
                        Flowable.just(v)
                                .subscribeOn(Schedulers.computation())
                                .map(w -> w * w)
                )
                .blockingSubscribe(System.out::println);*/

    }
}
