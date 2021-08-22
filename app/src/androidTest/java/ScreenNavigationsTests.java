import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.seefood.app.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ScreenNavigationsTests {
    @Test
    public void testEvent() {
            ActivityScenario scenario = ActivityScenario.launch(MainActivity.class);
            scenario.moveToState(Lifecycle.State.CREATED);
    }
}
