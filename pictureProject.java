import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.lang.Math;
public class pictureProject extends FlexiblePictureExplorer implements ImageObserver {
	private final String path = "images/";
	public pictureProject() {
		super(new Picture(1000,1000));
		Picture display = new Picture(1000,1000);
		Graphics2D graphics = display.createGraphics();
		Picture pict;
		pict = new Picture("images/black.jpg");
		graphics.drawImage(pict.getBufferedImage(), 0, 0, this);
		setImage(display);
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mouseClickedAction(DigitalPicture pict, Pixel pix) {
		if (pix.getRed()==0&&pix.getBlue()==0&&pix.getGreen()==0) {
			pix.setBlue((int)(Math.random()*255));
			pix.setRed((int)(Math.random()*255));
			pix.setGreen((int)(Math.random()*255));
		} 
	}
	public static void main(String[] args){
		new pictureProject();
	}
	
}