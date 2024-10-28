package com.example.scannerallerpro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private int[] images;
    private Context context;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private boolean isHolding = false;
    private OnImageHoldListener onImageHoldListener;

    // Interface for hold listener
    public interface OnImageHoldListener {
        void onHoldStart();
        void onHoldEnd();
    }

    // Constructor with hold listener
    public ImageSliderAdapter(int[] images, Context context) {
        this.images = images;
        this.context = context;
        this.onImageHoldListener = onImageHoldListener;

        // Initialize ScaleGestureDetector for pinch-to-zoom
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.8f, Math.min(scaleFactor, 3.0f)); // Limit zoom range
                return true;
            }
        });

        // Initialize GestureDetector for double-tap to reset scale
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                scaleFactor = 1.0f; // Reset scale factor to original size
                return true;
            }
        });
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_slider_item, parent, false);
        return new SliderViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.imageView.setImageResource(images[position]);

        // Set up touch listener for pinch-to-zoom, double-tap reset, and hold-to-zoom functionality
        holder.imageView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event); // Detect pinch gestures
            gestureDetector.onTouchEvent(event);      // Detect double-tap gestures

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isHolding = true;
                    if (onImageHoldListener != null) {
                        onImageHoldListener.onHoldStart(); // Notify start of hold
                    }
                    holder.imageView.postDelayed(() -> {
                        if (isHolding) { // Only zoom if still holding after delay
                            scaleFactor = 2.0f; // Increase scale factor to zoom in
                            holder.imageView.setScaleX(scaleFactor);
                            holder.imageView.setScaleY(scaleFactor);
                        }
                    }, 500); // Delay for 500 ms to detect hold
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isHolding = false; // Reset hold state
                    if (onImageHoldListener != null) {
                        onImageHoldListener.onHoldEnd(); // Notify end of hold
                    }
                    // Reset zoom on release
                    holder.imageView.setScaleX(1.0f);
                    holder.imageView.setScaleY(1.0f);
                    scaleFactor = 1.0f;
                    break;

                default:
                    break;
            }

            // Apply the scale factor to the ImageView in case of pinch gestures
            v.setScaleX(scaleFactor);
            v.setScaleY(scaleFactor);
            return true;
        });

        // Handle image click with a Toast message
        holder.imageView.setOnClickListener(v ->
                Toast.makeText(context, "Image clicked: " + images[position], Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SliderViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image);
        }
    }
}
