package io.github.mekhontsev.magicdesk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Rect;

import org.junit.Test;

public final class NativeWindowBoundsControllerTest {
    private static final Rect DISPLAY = rect(0, 0, 1920, 1080);
    private static final Rect WORK_AREA = rect(0, 0, 1920, 1016);
    private static final Rect PHONE_STABLE_AREA = rect(0, 125, 1216, 2623);
    private static final Rect PHONE_WORK_AREA = rect(0, 125, 1216, 2454);

    @Test
    public void halfAndQuarterSnapsPartitionTheUsableWorkArea() {
        final Rect area = rect(13, 125, 1216, 2454);
        assertBounds(NativeWindowBoundsController.snappedBounds(area, true, -1),
                13, 125, 614, 1289);
        assertBounds(NativeWindowBoundsController.snappedBounds(area, true, 0),
                13, 125, 614, 2454);
        assertBounds(NativeWindowBoundsController.snappedBounds(area, true, 1),
                13, 1289, 614, 2454);
        assertBounds(NativeWindowBoundsController.snappedBounds(area, false, -1),
                614, 125, 1216, 1289);
        assertBounds(NativeWindowBoundsController.snappedBounds(area, false, 0),
                614, 125, 1216, 2454);
        assertBounds(NativeWindowBoundsController.snappedBounds(area, false, 1),
                614, 1289, 1216, 2454);
        assertBounds(area, 13, 125, 1216, 2454);
    }

    @Test
    public void nativeLeftSnapUsesTaskbarWorkArea() {
        assertBounds(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(0, 0, 960, 1080),
                        DISPLAY,
                        WORK_AREA),
                0, 0, 960, 1016);
    }

    @Test
    public void nativeRightSnapUsesTaskbarWorkArea() {
        assertBounds(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(960, 0, 1920, 1080),
                        DISPLAY,
                        WORK_AREA),
                960, 0, 1920, 1016);
    }

    @Test
    public void fullStableHeightPreservesHorizontalBounds() {
        assertBounds(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(0, 125, 715, 2623),
                        PHONE_STABLE_AREA,
                        PHONE_WORK_AREA),
                0, 125, 715, 2454);
        assertBounds(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(608, 125, 1323, 2623),
                        PHONE_STABLE_AREA,
                        PHONE_WORK_AREA),
                608, 125, 1323, 2454);
        assertBounds(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(137, 125, 1048, 2623),
                        PHONE_STABLE_AREA,
                        PHONE_WORK_AREA),
                137, 125, 1048, 2454);
    }

    @Test
    public void existingWorkAreaSnapNeedsNoCorrection() {
        assertNull(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(0, 0, 960, 1016),
                        DISPLAY,
                        WORK_AREA));
    }

    @Test
    public void arbitraryResizeIsNotTreatedAsNativeSnap() {
        assertNull(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(0, 80, 960, 1080),
                        DISPLAY,
                        WORK_AREA));
        assertNull(
                NativeWindowBoundsController.correctNativeCaptionSnapBounds(
                        rect(100, 0, 1000, 1060),
                        DISPLAY,
                        WORK_AREA));
    }

    @Test
    public void phonePortraitFitUsesLiveAspectAndCentersInWideWorkArea() {
        final Rect area = rect(10, 20, 2010, 920);
        final Rect fitted = NativeWindowBoundsController.phonePortraitBounds(
                area, 2, 5);

        assertTrue(area.left <= fitted.left && fitted.right <= area.right);
        assertTrue(area.top <= fitted.top && fitted.bottom <= area.bottom);
        assertEquals(fitted.height() * 2 / 5, fitted.width());
        assertEquals(area.left + (area.width() - fitted.width()) / 2,
                fitted.left);
        assertEquals(area.top, fitted.top);
    }

    @Test
    public void phonePortraitFitUsesWidthWhenWorkAreaIsTooNarrow() {
        final Rect area = rect(40, 30, 340, 1030);
        final Rect fitted = NativeWindowBoundsController.phonePortraitBounds(
                area, 2, 5);

        assertEquals(area.width(), fitted.width());
        assertEquals(fitted.width() * 5 / 2, fitted.height());
        assertEquals(area.left, fitted.left);
        assertEquals(area.top + (area.height() - fitted.height()) / 2,
                fitted.top);
        assertTrue(area.left <= fitted.left && fitted.right <= area.right);
        assertTrue(area.top <= fitted.top && fitted.bottom <= area.bottom);
    }

    @Test
    public void phonePortraitFitIsRotationIndependentAndFailsClosedWithoutGeometry() {
        final Rect area = rect(10, 20, 2010, 920);
        assertEquals(
                NativeWindowBoundsController.phonePortraitBounds(area, 2, 5),
                NativeWindowBoundsController.phonePortraitBounds(area, 5, 2));
        assertNull(NativeWindowBoundsController.phonePortraitBounds(null, 2, 5));
        assertNull(NativeWindowBoundsController.phonePortraitBounds(
                rect(0, 0, 0, 100), 2, 5));
        assertNull(NativeWindowBoundsController.phonePortraitBounds(area, 0, 5));
    }

    private static void assertBounds(
            final Rect actual,
            final int left,
            final int top,
            final int right,
            final int bottom) {
        assertEquals(left, actual.left);
        assertEquals(top, actual.top);
        assertEquals(right, actual.right);
        assertEquals(bottom, actual.bottom);
    }

    private static Rect rect(
            final int left,
            final int top,
            final int right,
            final int bottom) {
        final Rect bounds = new Rect();
        bounds.left = left;
        bounds.top = top;
        bounds.right = right;
        bounds.bottom = bottom;
        return bounds;
    }
}
