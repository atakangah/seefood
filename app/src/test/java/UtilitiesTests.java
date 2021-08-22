import android.content.Context;

import com.seefood.app.overrides.GlideImageTransformation;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UtilitiesTests {

    @Mock
    Context mockContext;

    GlideImageTransformation glideTransformer = new GlideImageTransformation(mockContext, 90);

    public void shouldReturnTheCorrectTransformationOfImage() {

    }
}
