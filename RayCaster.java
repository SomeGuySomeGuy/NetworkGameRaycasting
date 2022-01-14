
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class RayCaster {

	private Graphics2D g2;
	private Level map;
	private double pX, pY;
	private Angle pAngle;
	private int xOffset, yOffset;
	private int wallType1;
	private int wallType2;
	private TextureList textures;

	public RayCaster(TextureList textures) {
		this.textures = textures;
	}

	public void rayCast(Graphics2D g2, boolean drawMap, double pX, double pY, Angle pAngle, int xOffset, int yOffset,
			Level map) {
		this.g2 = g2;
		this.map = map;
		this.pX = pX;
		this.pY = pY;
		this.pAngle = pAngle;
		this.xOffset = xOffset;
		this.yOffset = yOffset;

		int numberOfRays = 360;
		double dist1;
		double dist2;
		double fov = Math.toRadians(90);
		double rayAngle;
		double plane = Math.tan(fov / 2) * 2;
		double inc = plane / numberOfRays;

		for (int rays = 0; rays < numberOfRays; rays++) {

			rayAngle = Angle.checkLimit(pAngle.getAngleValue() + Math.atan(inc * (rays - numberOfRays / 2)));

			dist1 = rayCastVerticalSides(rayAngle);
			dist2 = rayCastHorizontalSides(rayAngle);

			boolean horizontal = false;

			if (dist1 > dist2 && dist2 != Const.NONE) { // hit veritcal side
				dist1 = dist2;
				wallType1 = wallType2;
			} else if (dist1 < dist2 && dist1 != Const.NONE) { // hit horizontal side
				horizontal = true;
			}

			double rayX = pX + dist1 * Math.cos(rayAngle);
			double rayY = pY - dist1 * Math.sin(rayAngle);

			if (drawMap) {

				g2.setColor(new Color(255, 165, 0));
				g2.setStroke(new BasicStroke(1));
				g2.drawLine((int) pX + xOffset, (int) pY + yOffset, (int) (pX + dist1 * Math.cos(rayAngle)) + xOffset,
						(int) (pY - dist1 * Math.sin(rayAngle) + yOffset));

			} else {

				wallType1--;

				dist1 = dist1 * Math.cos(rayAngle - pAngle.getAngleValue());

				int strokeWidth = Const.WIDTH / numberOfRays;
				g2.setStroke(new BasicStroke(strokeWidth));

				int numbPixel = textures.getTextureRows(wallType1);
				double lineH = (Const.HEIGHT * Const.BOXSIZE / 2) / dist1;
				double middle = lineH / 2;
				double stepPattern = (lineH / numbPixel);
				int textureX;
				int textureY = 0;

				if (!horizontal) {
					if (rayAngle < Math.PI) {
						textureX = (int) (rayX / (Const.BOXSIZE / textures.getTextureColumns(wallType1))
								% textures.getTextureColumns(wallType1));
					} else {
						textureX = numbPixel - (int) (rayX / (Const.BOXSIZE / textures.getTextureColumns(wallType1))
								% textures.getTextureColumns(wallType1)) - 1;
					}
				} else {
					if (rayAngle < Math.PI / 2 || rayAngle > 3 * Math.PI / 2) {
						textureX = (int) (rayY / (Const.BOXSIZE / textures.getTextureColumns(wallType1))
								% textures.getTextureColumns(wallType1));
					} else {
						textureX = numbPixel - (int) (rayY / (Const.BOXSIZE / textures.getTextureColumns(wallType1))
								% textures.getTextureColumns(wallType1)) - 1;
					}
				}

				if (lineH > Const.HEIGHT) {
					lineH = Const.HEIGHT;
				}

				for (int i = 0; i < numbPixel; i++) {
					textureY = (numbPixel - 1 - i);

					int value = textures.getTextureTile(wallType1, (int) textureY, (int) textureX);

					if (dist1 == Const.NONE || wallType1 < 0) {
						g2.setColor(Color.WHITE);
					} else if (value == 1 && horizontal) {
						g2.setColor(new Color(170, 110, 0));
					} else if (value == 0 && horizontal) {
						g2.setColor(new Color(255, 165, 0));
					} else if (value == 1 && !horizontal) {
						g2.setColor(new Color(64, 41, 0));
					} else if (value == 0 && !horizontal) {
						g2.setColor(new Color(127, 82, 0));
					}

					g2.drawLine(Const.WIDTH - rays * strokeWidth - 80,
							(int) (Const.HEIGHT / 2 + (middle) - i * stepPattern),
							Const.WIDTH - rays * strokeWidth - 80,
							(int) (Const.HEIGHT / 2 + (middle) - (i + 1) * stepPattern));

				}
			}
		}
	}

	public double rayCastVerticalSides(double rayAngle) {

		double distance = Const.NONE;
		double xStep;
		double yStep;
		double xInt;
		double yInt;
		int width = map.getColumns() * Const.BOXSIZE;
		int height = map.getRows() * Const.BOXSIZE;

		if (rayAngle == Math.PI / 2 || rayAngle == 3 * Math.PI / 2) {
			return distance;
		} else if (rayAngle < Math.PI / 2 || rayAngle > 3 * Math.PI / 2) { // right
			xInt = Const.BOXSIZE * (Math.ceil(pX / Const.BOXSIZE));
			yInt = pY - (xInt - pX) * Math.tan(rayAngle);
			xStep = Const.BOXSIZE;
			yStep = Math.tan(rayAngle) * xStep;
		} else { // left
			xInt = Const.BOXSIZE * (Math.floor(pX / Const.BOXSIZE));
			yInt = pY - (xInt - pX) * Math.tan(rayAngle);
			xStep = -Const.BOXSIZE;
			yStep = Math.tan(rayAngle) * xStep;
		}

		while (xInt < width && xInt >= 0 && yInt < height && yInt >= 0) {
			if (map.getMapTile((int) yInt / Const.BOXSIZE, (int) xInt / Const.BOXSIZE - 1) >= 1) {
				wallType1 = map.getMapTile((int) yInt / Const.BOXSIZE, (int) xInt / Const.BOXSIZE - 1);
				distance = distance(pX, pY, xInt, yInt);
				break;
			} else if (map.getMapTile((int) yInt / Const.BOXSIZE, (int) xInt / Const.BOXSIZE) >= 1) {
				wallType1 = map.getMapTile((int) yInt / Const.BOXSIZE, (int) xInt / Const.BOXSIZE);
				distance = distance(pX, pY, xInt, yInt);
				break;
			} else {
				xInt += xStep;
				yInt -= yStep;
			}
		}

		return distance;
	}

	public double rayCastHorizontalSides(double rayAngle) {
		double distance = Const.NONE;
		double xStep;
		double yStep;
		double xInt;
		double yInt;
		int width = map.getColumns() * Const.BOXSIZE;
		int height = map.getRows() * Const.BOXSIZE;

		if (rayAngle == Math.PI || rayAngle == 0 || rayAngle == 2 * Math.PI) {
			return distance;
		} else if (rayAngle < Math.PI) { // up
			yInt = Const.BOXSIZE * (Math.floor(pY / Const.BOXSIZE));
			xInt = pX + (yInt - pY) / Math.tan(-rayAngle);
			yStep = -Const.BOXSIZE;
			xStep = yStep / Math.tan(rayAngle);
		} else { // down
			yInt = Const.BOXSIZE * (Math.ceil(pY / Const.BOXSIZE));
			xInt = pX + (yInt - pY) / Math.tan(-rayAngle);
			yStep = Const.BOXSIZE;
			xStep = yStep / Math.tan(rayAngle);
		}

		while (xInt < width && xInt >= 0 && yInt < height && yInt >= 0) {
			if (map.getMapTile((int) yInt / Const.BOXSIZE - 1, (int) xInt / Const.BOXSIZE) >= 1) {
				wallType2 = map.getMapTile((int) yInt / Const.BOXSIZE - 1, (int) xInt / Const.BOXSIZE);
				distance = distance(pX, pY, xInt, yInt);
				break;
			} else if (map.getMapTile((int) yInt / Const.BOXSIZE, (int) xInt / Const.BOXSIZE) >= 1) {
				wallType2 = map.getMapTile((int) yInt / Const.BOXSIZE, (int) xInt / Const.BOXSIZE);
				distance = distance(pX, pY, xInt, yInt);
				break;
			} else {
				xInt -= xStep;
				yInt += yStep;
			}
		}

		return distance;
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

}