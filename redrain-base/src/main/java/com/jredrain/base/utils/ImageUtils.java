package com.jredrain.base.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * 图片剪切工具类
 *
 */
public class ImageUtils {
	
	/** 
     * 图像切割（改）     * 
     * @param srcImageFile            源图像地址
     * @param dirImageFile            新图像地址
     * @param x                       目标切片起点x坐标
     * @param y                      目标切片起点y坐标
     * @param destWidth              目标切片宽度
     * @param destHeight             目标切片高度
     */
    public static void abscut(String srcImageFile,String dirImageFile, int x, int y, int destWidth,int destHeight) {
        try {
            Image img;
            ImageFilter cropFilter;
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getWidth(); // 源图宽度
            int srcHeight = bi.getHeight(); // 源图高度          
            if (srcWidth >= destWidth && srcHeight >= destHeight) {
                Image image = bi.getScaledInstance(srcWidth, srcHeight,
                        Image.SCALE_SMOOTH);
                // 改进的想法:是否可用多线程加快切割速度
                // 四个参数分别为图像起点坐标和宽高
                // 即: CropImageFilter(int x,int y,int width,int height)
                cropFilter = new CropImageFilter(x, y, destWidth, destHeight);
                img = Toolkit.getDefaultToolkit().createImage(
                        new FilteredImageSource(image.getSource(), cropFilter));
                
//                destWidth = destWidth<100?destWidth:100;
//                destHeight = destHeight<100?destHeight:100;
                
                BufferedImage tag = new BufferedImage(destWidth, destHeight,  BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                g.dispose();
                // 输出为文件
                ImageIO.write(tag, "JPEG", new File(dirImageFile));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
	/**
	 * 缩放图像
	 * 
	 * @param srcImageFile       源图像文件地址
	 * @param result             缩放后的图像地址
	 * @param scale              缩放比例
	 * @param flag               缩放选择:true 放大; false 缩小;
	 */
	public static float scale(String srcImageFile, String result, int scale,boolean flag) {

		try {
			File file = new File(srcImageFile);
			BufferedImage src = ImageIO.read(file); // 读入文件
			int width = src.getWidth(); // 得到源图宽
			int height = src.getHeight(); // 得到源图长
			
			float n = width>height?width:height;
			float f = n>300f?n/300f:scale;

			if (flag) {
				// 放大
				width = width * scale;
				height = height * scale;
			} else {
				// 缩小
				width = (int) (width / f);
				height = (int) (height / f);
			}
			
			if(n > 300f){//如果大于300则缩小图片
				Image image = src.getScaledInstance(width, height,Image.SCALE_SMOOTH);
				BufferedImage tag = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
				Graphics g = tag.getGraphics();
				g.drawImage(image, 0, 0, null); // 绘制缩小后的图
				g.dispose();
				ImageIO.write(tag, "JPEG", new File(result));// 输出到文件流
			} else if (width>=140&&height>=140) {//宽高小与300,140.直接返回原图
				File targetFile = new File(result);
				FileOutputStream fos1=new FileOutputStream(targetFile);
				//对文件进行读操作
				FileInputStream fis=new FileInputStream(srcImageFile);
				byte[] buffer=new byte[1024];
				int len=0;
				//读入流，保存至byte数组
				while((len=fis.read(buffer))>0){
					fos1.write(buffer,0,len);
				}
				fos1.close();
				fis.close();
				f = 1f;
			}else {//宽或高有小于140的,需要裁剪....
				zoom(srcImageFile,result,140,140);
				f=-1f;//缩小。。
			}
			return f;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 1f;
	}
	
	/**
	 * 重新生成按指定宽度和高度的图像
	 * @param srcImageFile       源图像文件地址
	 * @param result             新的图像地址
	 * @param _width             设置新的图像宽度
	 * @param _height            设置新的图像高度
	 */
	public static void scale(String srcImageFile, String result, int _width,int _height) {		
		scale(srcImageFile,result,_width,_height,0,0);
	}
	
	public static void scale(String srcImageFile, String result, int _width,int _height,int x,int y) {
		try {
			
			BufferedImage src = ImageIO.read(new File(srcImageFile)); // 读入文件
			
			int width = src.getWidth(); // 得到源图宽
			int height = src.getHeight(); // 得到源图长
			
			if (width > _width) {
				 width = _width;
			}
			if (height > _height) {
				height = _height;
			}			
			Image image = src.getScaledInstance(width, height,Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, x, y, null); // 绘制缩小后的图
			g.dispose();	
			
//			FileOutputStream out = new FileOutputStream(result);        
//	        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);        
//	        JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(tag);  
            /* 压缩质量 */  
//            jep.setQuality(0.5f, true);  
//            encoder.encode(tag, jep);  
           /*近JPEG编码*/  
//	        out.close(); 
	        
			ImageIO.write(tag, "JPEG", new File(result));// 输出到文件流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 重新生成按指定宽度和高度的图像
	 * @param imageFile       源图像文件
	 * @param result             新的图像地址
	 * @param _width             设置新的图像宽度
	 * @param _height            设置新的图像高度
	 */
	public static void scale(File imageFile, String result, int _width,int _height,int x,int y) {
		try {
			
			BufferedImage src = ImageIO.read(imageFile); // 读入文件
			
			int width = src.getWidth(); // 得到源图宽
			int height = src.getHeight(); // 得到源图长
			
			if (width > _width) {
				 width = _width;
			}
			if (height > _height) {
				height = _height;
			}			
			Image image = src.getScaledInstance(width, height,Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, x, y, null); // 绘制缩小后的图
			g.dispose();			
			ImageIO.write(tag, "JPEG", new File(result));// 输出到文件流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 图像类型转换 GIF->JPG GIF->PNG PNG->JPG PNG->GIF(X)
	 */
	public static void convert(String source, String result) {
		try {
			File f = new File(source);
			f.canRead();
			f.canWrite();
			BufferedImage src = ImageIO.read(f);
			ImageIO.write(src, "JPG", new File(result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 彩色转为黑白
	 * 
	 * @param source
	 * @param result
	 */
	public static void gray(String source, String result) {
		try {
			BufferedImage src = ImageIO.read(new File(source));
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			ColorConvertOp op = new ColorConvertOp(cs, null);
			src = op.filter(src, null);
			ImageIO.write(src, "JPEG", new File(result));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * 老外写的图片缩放 支持jdk1.6 不支持jdk1.6以上版本
	 * @param srcImageFile
	 * @param result
	 * @param newWidth
	 * @param quality
	 * @throws IOException
	 */
	 public static void resize(String srcImageFile, String result,  
	            int newWidth, float quality) throws IOException {  
	  
		 File originalFile = new File(srcImageFile);
		 
	        if (quality > 1) {  
	            throw new IllegalArgumentException(  
	                    "Quality has to be between 0 and 1");  
	        }  
	  
	        ImageIcon ii = new ImageIcon(originalFile.getCanonicalPath());  
	        Image i = ii.getImage();  
	        Image resizedImage = null;  
	  
	        int iWidth = i.getWidth(null);  
	        int iHeight = i.getHeight(null);  
	  
	        if (iWidth > iHeight) {  
	            resizedImage = i.getScaledInstance(newWidth, (newWidth * iHeight)  
	                    / iWidth, Image.SCALE_SMOOTH);  
	        } else {  
	            resizedImage = i.getScaledInstance((newWidth * iWidth) / iHeight,  
	                    newWidth, Image.SCALE_SMOOTH);  
	        }  
	  
	        // This code ensures that all the pixels in the image are loaded.  
	        Image temp = new ImageIcon(resizedImage).getImage();  
	  
	        // Create the buffered image.  
	        BufferedImage bufferedImage = new BufferedImage(temp.getWidth(null),  
	                temp.getHeight(null), BufferedImage.TYPE_INT_RGB);  
	  
	        // Copy image to buffered image.  
	        Graphics g = bufferedImage.createGraphics();  
	  
	        // Clear background and paint the image.  
	        g.setColor(Color.white);  
	        g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));  
	        g.drawImage(temp, 0, 0, null);  
	        g.dispose();  
	  
	        // Soften.  
	        float softenFactor = 0.05f;  
	        float[] softenArray = { 0, softenFactor, 0, softenFactor,  
	                1 - (softenFactor * 4), softenFactor, 0, softenFactor, 0 };  
	        Kernel kernel = new Kernel(3, 3, softenArray);  
	        ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);  
	        bufferedImage = cOp.filter(bufferedImage, null);  
	  
	        // Write the jpeg to a file.  
//	        FileOutputStream out = new FileOutputStream(result);  
	  
	        // Encodes image as a JPEG data stream  
//	        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);  
//	  
//	        JPEGEncodeParam param = encoder  
//	                .getDefaultJPEGEncodeParam(bufferedImage);  
//	  
//	        param.setQuality(quality, true);  
	  
//	        encoder.setJPEGEncodeParam(param);  
//	        encoder.encode(bufferedImage);  
	    } 
	
	 /**
		 * 图片缩放(图片等比例缩放为指定大小，空白部分以白色填充)
		 * 
		 * @param srcImageFile
		 *            源图片
		 * @param result
		 *            缩放后的图片文件
		 */
		public static void zoom(String srcImageFile, String result, int destHeight, int destWidth) {
			try {
				BufferedImage srcBufferedImage = ImageIO.read(new File(srcImageFile));
				int imgWidth = destWidth;
				int imgHeight = destHeight;
				int srcWidth = srcBufferedImage.getWidth();
				int srcHeight = srcBufferedImage.getHeight();
				if (srcHeight >= srcWidth) {
					imgWidth = (int) Math.round(((destHeight * 1.0 / srcHeight) * srcWidth));
				} else {
					imgHeight = (int) Math.round(((destWidth * 1.0 / srcWidth) * srcHeight));
				}
				BufferedImage destBufferedImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics2D = destBufferedImage.createGraphics();
				graphics2D.setBackground(Color.WHITE);
				graphics2D.clearRect(0, 0, destWidth, destHeight);
				graphics2D.drawImage(srcBufferedImage.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH), (destWidth / 2) - (imgWidth / 2), (destHeight / 2) - (imgHeight / 2), null);
				graphics2D.dispose();
				ImageIO.write(destBufferedImage, "JPEG", new File(result));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 处理透明图片
		 * 
		 * @param srcImageFile
		 *            源图片
		 */
		public static void zoom(File srcImageFile) {
			try {
				BufferedImage srcBufferedImage = ImageIO.read(srcImageFile);
				int srcWidth = srcBufferedImage.getWidth();
				int srcHeight = srcBufferedImage.getHeight();
				BufferedImage destBufferedImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics2D = destBufferedImage.createGraphics();
				graphics2D.setBackground(Color.WHITE);
				graphics2D.clearRect(0, 0, srcWidth, srcHeight);
				graphics2D.drawImage(srcBufferedImage.getScaledInstance(srcWidth, srcHeight, Image.SCALE_SMOOTH), (srcWidth / 2) - (srcWidth / 2), (srcHeight / 2) - (srcHeight / 2), null);
				graphics2D.dispose();
				ImageIO.write(destBufferedImage, "JPEG", new File(srcImageFile.getPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

}
