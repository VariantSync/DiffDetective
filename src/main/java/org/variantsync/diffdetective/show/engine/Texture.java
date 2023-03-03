package org.variantsync.diffdetective.show.engine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * This class is used to represent, load and redraw
 * the Images of the entities and the world.
 *
 * The upper left corner of the Texture has the coordinates (0/0).
 * The lower right corner of the Texture has the coordinates (getWidth()/getHeight()).
 * @author Paul Maximilian Bittner
 */
public class Texture implements Serializable
{
    /**Fields**/

    public static final Color clr_TRANSPARENT = new Color(0,0,0,0);
    public static final int HD_WIDTH = 1920;
    public static final int HD_HEIGHT = 1080;

    /**instance values**/

    private BufferedImage image;
    private Color drawingColor;
    private Font font;

    /**Constructors**/

    /**
     * Creates a new Texture of the given size in pixels.
     */
    public Texture(int width, int height){
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        init();
    }

    /**
     * Creates a new Texture from the given path.
     */
    public Texture(String path){
        image = loadImage(new File(path));
        init();
    }

    /**
     * Creates a new Texture from the given file.
     */
    public Texture(File file){
        image = loadImage(file);
        init();
    }

    /**
     * Creates a copy of the given texture.
     */
    public Texture(Texture texture) {
        this.image = copy(texture);
        init();
    }

    /**
     * Creates a new Texture from a BufferedImage.
     */
    public Texture(BufferedImage image){
        this.image = copy(image);
        init();
    }

    /**
     * Creates a new Texture, that is only text.
     * @param text the String that should be drawn as this Texture
     * @param height the height of the text in pixels
     * @param foreground the color of the text
     * @param background the color behind the text
     */
    public Texture(String text, int height, Color foreground, Color background){
        init();
        Font copiedFont = font.deriveFont(font.getStyle(), height);
        Rectangle2D rect = copiedFont.getStringBounds(text, new FontRenderContext(new AffineTransform(), false, true));
        image = new BufferedImage((int)(rect.getWidth()*1.05), copiedFont.getSize(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setFont(copiedFont);
        g2.setColor(background);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2.setColor(foreground);
        g2.drawString(text, 0, image.getHeight());
    }

    /**
     * Creates a new Texture, that is only text.
     * @param text the String that should be drawn as this Texture
     * @param height the height of the text in pixels
     * @param foreground the color of the text
     * @param background the color behind the text
     * @param font the Font for the text
     */
    public Texture(String text, int height, Color foreground, Color background, Font font){
        init();
        if(font == null)
            throw new IllegalArgumentException("Font cannot be null!");
        this.font = font;
        Rectangle2D rect = font.getStringBounds(text, new FontRenderContext(new AffineTransform(), false, true));
        image = new BufferedImage((int)(rect.getWidth()*1.05), (int)(font.getSize()*1.2), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setFont(font);
        g2.setColor(background);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2.setColor(foreground);
        g2.drawString(text, 0, image.getHeight() - (image.getHeight()-font.getSize())/2);
    }

    /**
     * Creates a new Texture from the given path.
     */
    public static Texture CreateFromFile(String path){
        return new Texture(loadImage(new File(path)));
    }

    /**
     * Initializes the values.
     */
    private void init(){
        drawingColor = Color.BLACK;
        font = new Font(Font.DIALOG, Font.PLAIN, 20);
    }

    private BufferedImage copy(Texture texture) {
        BufferedImage copy = new BufferedImage(texture.getWidth(), texture.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.createGraphics().drawImage(texture.getAwtImage(), null, 0, 0);
        return copy;
    }

    private BufferedImage copy(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.createGraphics().drawImage(image, null, 0, 0);
        return copy;
    }

    /**image change methods**/

    /**
     * This method makes images becoming more or less transparent.
     * @param value The intensity of transparency. It has to be between 0 and 255 (including both). (It represents the alpha value of a RGB-color.)
     */
    public void setTransparency(int value){
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                Color color = new Color(image.getRGB(x, y));
                Color next = new Color(color.getBlue(), color.getRed(), color.getGreen(), value);
                image.setRGB(x, y, next.getRGB());
            }
        }
    }

    /**
     * The Texture will be resized by the given factor, that means, that the
     * width and height of the Texture will be multiplied with factor:
     *
     * @param factor The factor the width and the height of the texture will be multiplied with.
     * If factor is smaller than 1 the Texture will be shrinked.
     * If factor is larger than 1 the Texture will grow.
     * If factor is 1 it won't change.
     */
    public void scale(double factor){
        Graphics2D g2 = image.createGraphics();
        BufferedImage sclBI = new BufferedImage((int)(image.getWidth()*factor), (int)(image.getHeight()*factor), BufferedImage.TYPE_INT_ARGB);
        Graphics2D sclG2 = sclBI.createGraphics();
        sclG2.scale(factor, factor);
        sclG2.drawImage(image, null, 0, 0);
        image = sclBI;
    }

    /**
     * The Texture will be resized to the given width and height.
     * @param width the new width of the texture
     * @param height the new height of the texture
     */
    public void scale(int width, int height){
        Graphics2D g2 = image.createGraphics();
        BufferedImage sclBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sclG2 = sclBI.createGraphics();
        sclG2.scale((double)width/(double)getWidth(), (double)height/(double)getHeight());
        sclG2.drawImage(image, null, 0, 0);
        image = sclBI;
    }

    /**
     * The Texture will be mirrored by a horizontal axis, that goes through
     * the center of the Texture.
     */
    public void mirrorHorizontally(){
        Graphics2D g2 = image.createGraphics();
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2C = copy.createGraphics();
        for(int y = 0; y < image.getHeight(); y++){
            g2C.drawImage(
                    image.getSubimage(0, y, image.getWidth(), 1),
                    null,
                    0, image.getHeight() - 1 - y);
        }
        image = copy;
    }

    /**
     * The Texture will be mirrored by a vertical axis, that goes through
     * the center of the Texture.
     */
    public void mirrorVertically(){
        Graphics2D g2 = image.createGraphics();
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2C = copy.createGraphics();
        for(int x = 0; x < image.getWidth(); x++){
            g2C.drawImage(
                    image.getSubimage(x, 0, 1, image.getHeight()),
                    null,
                    image.getWidth() - 1 - x, 0);
        }
        image = copy;
    }

    /**
     * The size of the returned Texture may be different to the original size, to be able to display
     * the whole rotated image, and not only a part of it.
     * @param degrees The amount of degrees, the Texture should be rotated to the right.
     * @return A Texture, that is this texture, rotated by the given degrees.
     */
    public Texture rotate(double degrees){
        int w = getWidth(), h = getHeight();
        double r = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(r)), cos = Math.abs(Math.cos(r));

        BufferedImage rotated = new BufferedImage(
                (int)( h*sin + w*cos ),
                (int)( h*cos + w*sin ),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D turnedG = (Graphics2D) rotated.createGraphics();
        turnedG.rotate(r,
                rotated.getWidth()/2,
                rotated.getHeight()/2
        );

        turnedG.drawImage(image, null,
                (rotated.getWidth()  - image.getWidth()) /2,
                (rotated.getHeight() - image.getHeight())/2
        );

        return new Texture(rotated);
    }

    /**
     * Cuts out the given rectangle, meaning, that this rectangle will be filled transparent.
     * NOT IMPLEMENTED
     */
    public Texture cut(int x, int y, int width, int height) {
        if (x > getWidth() || y > getHeight())
            return null;

        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;

        if (x + width >= getWidth())
            width = getWidth() - x;
        if (y + height >= getHeight())
            height = getHeight() - y;


        Texture t = new Texture(this.image.getSubimage(x, y, width, height));

        Graphics2D g2 = this.image.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(x, y, width, height);

        return t;
    }

    /**draw methods**/

    /**
     * Draws a line onto the Texture from P(x1/y1) to Q(x2/y2)
     * with the current drawing color.
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     */
    public void drawLine(int x1, int y1, int x2, int y2){
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.drawLine(x1, y1, x2, y2);
    }

    /**
     * Draws a line onto the Texture from pointA to pointB
     * with the current drawing color.
     * @param pointA The start point of the line.
     * @param pointB The end point of the line.
     */
    public void drawLine(Point pointA, Point pointB){
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.drawLine((int) pointA.getX(), (int) pointA.getY(), (int) pointB.getX(), (int) pointB.getY());
    }

    /**
     * Draws a rectangle with the current drawing color onto the Texture.
     * @param x the x coordinate of the upper left corner of the rectangle
     * @param y the y coordinate of the upper left corner of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public void drawRect(int x, int y, int width, int height){
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.drawRect(x, y, width, height);
    }

    /**
     * Draws an Oval with the current drawing color onto the Texture.
     * @param x the x coordinate of the upper left corner of the oval
     * @param y the y coordinate of the upper left corner of the oval
     * @param width the width of the oval
     * @param height the height of the oval
     */
    public void drawOval(int x, int y, int width, int height){
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.drawOval(x, y, width, height);
    }

    /**
     * Draws a closed polygon defined by arrays of x and y coordinates.
     * The figure is automatically closed by drawing a line connecting the final point to the first point,
     * if those points are different.
     * @param xPoints the x coordinates of the corner points
     * @param yPoints the y coordinates of the corner points
     */
    public void drawPolygon(int[] xPoints, int[] yPoints){
        if(xPoints.length != yPoints.length)
            throw new IllegalArgumentException("xPoints.length has to be equal to yPoints.length");
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.drawPolygon(xPoints, yPoints, xPoints.length);
    }

    /**
     * Draws a closed polygon defined by the given array of points.
     * The figure is automatically closed by drawing a line connecting the final point to the first point,
     * if those points are different.
     * @param points The points of the polygon in the correct order.
     */
    public void drawPolygon(Point[] points){
        if(points == null)
            throw new IllegalArgumentException("The points can't be null!");
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        int[] x = new int[points.length];
        int[] y = new int[points.length];
        for(int i = 0; i < points.length; i++){
            x[i] = (int) points[i].getX();
            y[i] = (int) points[i].getY();
        }
        g2.drawPolygon(x, y, points.length);
    }

    /**
     * Draws the given Texture onto this Texture at the given location.
     * The coordinate describes the upper left corner of texture.
     * @param texture the Texture, that should be painted onto this one
     * @param x the x coordinate of the upper left corner of texture in this Texture
     * @param y the y coordinate of the upper left corner of texture in this Texture
     */
    public void drawTexture(Texture texture, int x, int y){
        Graphics2D g2 = image.createGraphics();
        g2.scale(1, 1);
        g2.drawImage(texture.getAwtImage(), null, x, y);
    }

    /**
     * Draws a String onto this image at the location P(x/y) with the given
     * Font and color.
     * @param text the String, that should be written onto this texture
     * @param x the x coordinate of the lower left corner of text
     * @param y the y coordinate of the lower left corner of text
     */
    public void drawString(String text, int x , int y){
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.setFont(font);
        g2.drawString(text, x, y - font.getSize());
    }

    /**fill methods**/

    /**
     * Fills the Texture with the current drawing color.
     */
    public void fill(){
        fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Draws a Rectangle onto the Texture and fills it with the
     * current drawing color.
     * @param x the x coordinate of the upper left corner of the rectangle
     * @param y the y coordinate of the upper left corner of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public void fillRect(int x, int y, int width, int height){
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.fillRect(x, y, width, height);
    }

    /**
     * Draws an Oval with the current drawing color onto the Texture and fills it.
     * @param x the x coordinate of the upper left corner of the oval
     * @param y the y coordinate of the upper left corner of the oval
     * @param width the width of the oval
     * @param height the height of the oval
     */
    public void fillOval(int x, int y, int width, int height){
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.fillOval(x, y, width, height);
    }

    /**
     * Fills a closed polygon defined by arrays of x and y coordinates.
     * The figure is automatically closed by drawing a line connecting the final point to the first point,
     * if those points are different.
     * @param xPoints the x coordinates of the corner points
     * @param yPoints the y coordinates of the corner points
     */
    public void fillPolygon(int[] xPoints, int[] yPoints){
        if(xPoints.length != yPoints.length)
            throw new IllegalArgumentException("xPoints.length has to be equal to yPoints.length");
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        g2.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    /**
     * Fills a closed polygon defined by the given array of points.
     * The figure is automatically closed by drawing a line connecting the final point to the first point,
     * if those points are different.
     * @param points The points of the polygon in the correct order.
     */
    public void fillPolygon(Point[] points){
        if(points == null)
            throw new IllegalArgumentException("The points can't be null!");
        Graphics2D g2 = image.createGraphics();
        g2.setColor(drawingColor);
        int[] x = new int[points.length];
        int[] y = new int[points.length];
        for(int i = 0; i < points.length; i++){
            x[i] = (int) points[i].getX();
            y[i] = (int) points[i].getY();
        }
        g2.fillPolygon(x, y, points.length);
    }

    /**setters**/

    /**
     * @param color the color used to draw
     */
    public void setColor(Color color){
        drawingColor = color;
    }

    /**
     * Paints the pixel at the location p(x/y) with color.
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param color the color for the pixel
     */
    public void setColorAt(int x, int y, Color color){
        image.setRGB(x, y, color.getRGB());
    }

    /**
     * Sets the current Font for drawing Strings.
     * @param font the font for drawing strings
     */
    public void setFont(Font font){
        this.font = font;
    }

    public void setAwtImage(BufferedImage image) {
        this.image = image;
    }

    public void set(Texture other) {
        this.image = copy(other.getAwtImage());
    }

    /**getters**/

    /**
     * @return the color of the pixel at p(x/y)
     */
    public Color getColorAt(int x, int y){
        return new Color(image.getRGB(x, y), true /** has alpha **/);
    }

    /**
     * @return the width of the Texture in pixels
     */
    public int getWidth(){
        return image.getWidth();
    }

    /**
     * @return the height of the Texture in pixels
     */
    public int getHeight(){
        return image.getHeight();
    }

    /**
     * @return the BufferedImage, that represents this Texture
     */
    public BufferedImage getAwtImage(){
        return image;
    }

    /**
     * @return the current Font for drawing Strings
     */
    public Font getFont(){
        return font;
    }

    /**
     * @return the color used to draw
     */
    public Color getColor(){
        return drawingColor;
    }

    public boolean contains(int x, int y) {
        return 0 <= x && x < getWidth() && 0 <= y && y < getHeight();
    }

    /**effects*/

    /**
     * @return a Texture, that represents the red part of this Texture of the RGB spectrum
     */
    public Texture redPart(){
        Texture copy = new Texture(getWidth(), getHeight());
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                Color o = getColorAt(x, y);
                Color n = new Color(o.getRed(), 0, 0, o.getAlpha());
                copy.setColorAt(x, y, n);
            }
        }
        return copy;
    }

    /**
     * @return a Texture, that represents the green part of this Texture of the RGB spectrum
     */
    public Texture greenPart(){
        Texture copy = new Texture(getWidth(), getHeight());
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                Color o = getColorAt(x, y);
                Color n = new Color(0, o.getGreen(), 0, o.getAlpha());
                copy.setColorAt(x, y, n);
            }
        }
        return copy;
    }

    /**
     * @return a Texture, that represents the blue part of this Texture of the RGB spectrum
     */
    public Texture bluePart(){
        Texture copy = new Texture(getWidth(), getHeight());
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                Color o = getColorAt(x, y);
                Color n = new Color(0, 0, o.getBlue(), o.getAlpha());
                copy.setColorAt(x, y, n);
            }
        }
        return copy;
    }

    /**
     * @param blackTop It's used to distinguish between black and white.
     * It has to be between 0 and 255 (including both).
     * As higher blackTop is as darker the Texture gets.
     *
     * @return a Texture, that is the contrast of this Texture
     */
    public Texture contrast(int blackTop){
        Texture copy = new Texture(getWidth(), getHeight());
        Color c, n;
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                c = getColorAt(x,y);
                if((c.getRed()   > blackTop &&
                        c.getGreen() > blackTop &&
                        c.getBlue()  > blackTop &&
                        c.getAlpha() > blackTop))
                    n = Color.WHITE;
                else
                    n = Color.BLACK;
                copy.setColorAt(x, y, n);
            }
        }
        return copy;
    }

    /**
     * @return a new Texture, that is this Texture filtered to black and white
     */
    public Texture blackAndWhite(){
        BufferedImage copy = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                int RGB = image.getRGB(x, y);
                Color o = new Color(RGB);
                int clrAvg = (int) ((o.getRed() + o.getGreen() + o.getBlue()) / 3);
                Color n = new Color(clrAvg, clrAvg, clrAvg, o.getAlpha());
                copy.setRGB(x, y, n.getRGB());
            }
        }
        return new Texture(copy);
    }

    /**
     * @return a new Texture, that is the negative of this Texture
     */
    public Texture negative(){
        Texture copy = new Texture(getWidth(), getHeight());
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                Color o = getColorAt(x,y);
                Color n = new Color(
                        255 - o.getRed(),
                        255 - o.getGreen(),
                        255 - o.getBlue(),
                        o.getAlpha()
                );
                copy.setColorAt(x,y,n);
            }
        }
        return copy;
    }

    /**
     * @return a new Texture, that is this Texture pixeled by the given parameter.
     * @param pixelLength the length of a pixel in the finished image in pixels
     */
    public Texture pixelize(int pixelLength){
        Texture copy = new Texture(getWidth(), getHeight());
        for(int y = pixelLength/2; y < getHeight()+pixelLength/2; y+=pixelLength){
            for(int x = pixelLength/2; x < getWidth()+pixelLength/2; x+=pixelLength){
                Color o;
                if(y < getHeight() && x < getWidth())
                    o = getColorAt(x,y);
                else if(y < getHeight() && x >= getWidth())
                    o = getColorAt(getWidth()-1, y);
                else if(y >= getHeight() && x < getWidth())
                    o = getColorAt(x, getHeight()-1);
                else
                    o = getColorAt(getWidth()-1, getHeight()-1);
                for(int yP = 0; yP < pixelLength && y+yP-pixelLength/2 < getHeight(); yP++)
                    for(int xP = 0; xP < pixelLength && x+xP-pixelLength/2 < getWidth(); xP++)
                        copy.setColorAt(x + xP - pixelLength/2, y + yP - pixelLength/2, o);
            }
        }
        return copy;
    }

    /**
     * @return A Texture, which colors equal to the parameter are changed to transparent.
     */
    public Texture deleteColor(Color color){
        return changeColor(color, clr_TRANSPARENT);
    }

    /**
     * @return A Texture, which colors equal to the parameter are changed to the other one.
     */
    public Texture changeColor(Color from, Color to){
        BufferedImage copy = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        int rgb = from.getRGB();
        for(int y = 0; y < getHeight(); y++){
            for(int x = 0; x < getWidth(); x++){
                if(image.getRGB(x, y) != rgb)
                    copy.setRGB(x, y, image.getRGB(x,y));
                else
                    copy.setRGB(x, y, to.getRGB());
            }
        }
        return new Texture(copy);
    }

    /**static methods**/

    /**
     * This method loads an image from the given file.
     * @return a java.awt.image.BufferdImage representing the image in file.
     */
    private static BufferedImage loadImage(File file){
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
            if(img == null)
                throw new IllegalArgumentException("The image file "+file.toString()+" isn't a supported image file!");
        } catch (IOException e) {
            throw new IllegalArgumentException("The image file "+file.toString()+" could not be found!");
        }
        return img;
    }

    /**
     * @param filename The directory of the font. It should be placed in "Resources/Fonts/". Otherwise it can't be found.
     * @return the font from the file, null if it couldn't be found or loaded
     */
    public static Font loadFont(String filename){
        Font font = null;
        try{
            font = Font.createFont(Font.TRUETYPE_FONT , new File(filename));
        }catch(java.awt.FontFormatException f){}
        catch(java.io.IOException ioe){}
        return font;
    }

    /**
     * @param filename The directory of the font. It should be placed in "Resources/Fonts/". Otherwise it can't be found.
     * @param xOffset The width  of one image on the sprite sheet.
     * @param yOffset The height of one image on the sprite sheet.
     * @return All Texture in the sprite sheet in a Texture array.
     */
    public static Texture[] loadSpriteSheet(String filename, int xOffset, int yOffset){
        BufferedImage sheet = loadImage(new File(filename));
        if(sheet == null)
            throw new IllegalArgumentException("The file couldn't be found!");
        Texture[] textures = new Texture[(sheet.getWidth()/xOffset) * (sheet.getHeight()/yOffset)];
        for(int y = 0; y < sheet.getHeight(); y += yOffset){
            for(int x = 0; x < sheet.getWidth(); x += xOffset){
                BufferedImage clip = sheet.getSubimage(x, y, xOffset, yOffset);
                textures[(x/xOffset) + ((y/yOffset) * (sheet.getWidth()/xOffset))] = new Texture(clip);
            }
        }
        return textures;
    }

    /**
     * The texture will be saved with the given name as a png-file
     * into the screenshots folder.
     * @param texture the texture, that should be saved
     * @param name the name the file, in which the texture is saved, should have
     */
    public static void saveAsPng(Texture texture, String name){
        try {
            File outputfile = new File(name + ".png");
            ImageIO.write(texture.getAwtImage(), "png", outputfile);
        } catch (IOException e) {}
    }

    /**
     * The texture will be saved with the given name as a png-file
     * into the screenshots folder.
     * @param texture the texture, that should be saved
     * @param file the file, in which the texture is saved
     */
    public static void saveAsPng(Texture texture, File file){
        try {
            ImageIO.write(texture.getAwtImage(), "png", file);
        } catch (IOException e) {}
    }

    public boolean inHeight(int y) {
        return 0 <= y && y < getHeight();
    }

    public boolean inWidth(int x) {
        return 0 <= x && x < getWidth();
    }
}