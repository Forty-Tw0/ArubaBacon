package lilium.arubabacon;

import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static lilium.arubabacon.MainActivity.beaconKeeper;
import static lilium.arubabacon.MainActivity.map;

class PositionUpdater {
    private long lastUpdate;
    private long maxUpdate;
    private Thread positionUpdate;
    private AtomicBoolean stop;
    private AtomicInteger running;

    private final long MaxSpawnWait = 100;


    public PositionUpdater(final long Maximum_Update) {
        lastUpdate = System.currentTimeMillis();
        maxUpdate = Maximum_Update;
        stop = new AtomicBoolean(false);
        running = new AtomicInteger(0);
        positionUpdate = new Thread(new Runnable(){
            public void run(){
                while(! stop.get()) {
                    if (running.compareAndSet(0,1)) { // is a async position updater running?
                        if (System.currentTimeMillis() - lastUpdate > Maximum_Update) {
                            new PositionUpdate().execute(beaconKeeper.clonePlaced());
                           try{
                                Thread.sleep(MaxSpawnWait);
                            } catch (InterruptedException e){
                                Log.e("PositionThread","Interrupted Exception");
                                e.printStackTrace();
                            }
                        }
                        else{
                            running.decrementAndGet();
                        }
                    }
                }
            }

        }, "PositionThread");
        positionUpdate.start();
    }

    private void updatePosition(ArrayList<iBeacon> beacons) {
        double[][] positions = new double[beacons.size()][2];
        double[] distances = new double[beacons.size()];
        if (beacons.size() > 1) {
            for (int i = 0; i < beacons.size(); i++) {
                positions[i][0] = beacons.get(i).x;
                positions[i][1] = beacons.get(i).y;
                //we want linear distances, the distance readings don't have to be accurate
                //they just need to be consistent across all beacons
                //because the trilateration function uses them as relative to each other
                distances[i] = beacons.get(i).distance();
            }
            try {
                NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
                LeastSquaresOptimizer.Optimum optimum = solver.solve();
                double[] calculatedPosition = optimum.getPoint().toArray();
                MainActivity.position = new PointF((float) calculatedPosition[0], (float) calculatedPosition[1]);
                lastUpdate = System.currentTimeMillis();
                try{
                    Thread.sleep(maxUpdate);
                } catch (InterruptedException e){
                    Log.e("async_position","Interrupted Exception");
                }
            } catch (TooManyEvaluationsException e) {
                // position stays the same
                Log.e("async_position", "TOO MANY CALCULATIONS");
            }
        }
    }

    private class PositionUpdate extends AsyncTask<Object,Void,Void> {

        protected Void doInBackground(Object ... args) {
            ArrayList<iBeacon> beacons;
            beacons = (ArrayList<iBeacon>) args[0];
            updatePosition(beacons);
            return null;
        }

        protected void onPostExecute(Void result) {
            map.invalidate();
            running.decrementAndGet();
        }
    }
}


