package kr.rokoroku.mbus.util;

/**
 * Created by rok on 2015. 7. 14..
 */
public interface ProgressCallback {
    void onComplete(boolean success);

    void onProgressUpdate(int current, int target);

    void onError(int progress, Throwable t);

    class ProgressRunner {

        private int current = 0;
        private int target;
        private boolean error = false;
        private boolean complete = false;
        private boolean runOnUiThread = true;
        private ProgressCallback progressCallback;

        public ProgressRunner(ProgressCallback progressCallback, int target) {
            this.progressCallback = progressCallback;
            setTarget(target);
        }

        public ProgressRunner(ProgressCallback progressCallback, int target, boolean runOnUiThread) {
            this.progressCallback = progressCallback;
            this.runOnUiThread = runOnUiThread;
            setTarget(target);
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current - 1;
            progress();
        }

        public int getTarget() {
            return target;
        }

        public synchronized void setTarget(int target) {
            this.target = target;
            checkComplete();
        }

        public boolean hasError() {
            return !error;
        }

        public synchronized void error(Throwable t) {
            if (progressCallback != null) {
                run(() -> progressCallback.onError(current + 1, t));
            }
            error = true;
            progress();
        }

        public synchronized void progress() {
            current++;
            if (progressCallback != null) {
                run(() -> progressCallback.onProgressUpdate(current, target));
            }
            checkComplete();
        }

        public synchronized void end(boolean success) {
            current = target;
            error = !success;
            if (progressCallback != null) {
                run(() -> progressCallback.onProgressUpdate(current, target));
            }
            checkComplete();
        }

        private void checkComplete() {
            if (current >= target && !complete) {
                if (progressCallback != null) {
                    run(() -> progressCallback.onComplete(!error));
                }
                complete = true;
            }
        }

        private void run(Runnable runnable) {
            if (runOnUiThread) {
                ViewUtils.runOnUiThread(runnable);
            } else {
                runnable.run();
            }
        }
    }
}
