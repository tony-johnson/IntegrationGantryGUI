package org.lsst.ccs.integrationgantrygui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * Simple component for displaying a buffered image
 *
 * @author tonyj
 */
public class ImageComponent extends JComponent {

    private BufferedImage image;

    public ImageComponent() {
        image = null;
    }

    ImageComponent(BufferedImage image) {
        setImage(image);
    }

    final void setImage(BufferedImage image) {

        this.image = image;
        repaint();
    }

    BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.scale(1, -1);
            g2.translate(0, -getHeight());
            long start = System.currentTimeMillis();
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            long stop = System.currentTimeMillis();
            System.out.printf("Paint image of type %d and size %dx%d took %dms\n", image.getType(), image.getWidth(), image.getHeight(), stop - start);
        }
    }
}
