package lilium.arubabacon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

//https://github.com/davemorrissey/subsampling-scale-image-view
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import static lilium.arubabacon.MainActivity.beacons;

public class DrawableImageView extends SubsamplingScaleImageView {
    Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    Paint p = new Paint();

    public DrawableImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(int i = 0; i < beacons.size(); i++) {
            PointF offset = sourceToViewCoord(beacons.get(i).x, beacons.get(i).y);
            if (offset != null) {
                Matrix matrix = new Matrix();
                matrix.postTranslate(offset.x, offset.y);
                canvas.drawBitmap(b, matrix, p);
            }
        }

        PointF offset = sourceToViewCoord(MainActivity.position.x, MainActivity.position.y);
        if (offset != null) {
            Matrix matrix = new Matrix();
            matrix.postTranslate(offset.x, offset.y);
            canvas.drawBitmap(b, matrix, p);
        }
    }
}
