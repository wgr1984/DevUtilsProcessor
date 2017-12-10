package de.wr.compilesafereflections;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import static java.util.Collections.emptyList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.wr.annotationprocessor.processor.DevUtils;
import de.wr.compilesafereflections.databinding.ActivityMainBinding;
import de.wr.libsimplecomposition.Debug;
import de.wr.libsimplecomposition.RemovedUntilVersion;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class MainActivity extends AppCompatActivity {
    private Disposable disposable;
    private ActivityMainBinding binding;
//    @Inject
//    Sample sampleField;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @RemovedUntilVersion("1.1.1")
    private void deprecatedUntil() {
        System.out.println("This method is about to expire");
    }

    @Debug(methodPattern = "debug_.*")
    private Object debug_test() {
        System.out.println("This is a debug method");
        return "Should not be seen !";
    }

    @Debug(allowNonPrivate = true)
    static List<String> testDebugMethod() {
        System.out.println("This is a debug method");
        return emptyList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dispose();
        disposable = Maybe.just("test")
                .flatMap(s -> Single.just("hallo " + s)
                        .delay(2, TimeUnit.SECONDS)
                        .toMaybe()
                        )
                .filter(x -> x.contains("t"))
                .observeOn(mainThread())
                .subscribe(
                    success -> {
                        Toast.makeText(this, "Success:" + success, Toast.LENGTH_LONG).show();
                    }, error -> {
                        Toast.makeText(this, "Error:" + error, Toast.LENGTH_LONG).show();
                    }
                 );
        if (DevUtils.IS_DEBUG) {
            testDebugMethod();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dispose();
    }

    private void dispose() {
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
