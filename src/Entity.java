
public class Entity
{
	public double x, y, z;
	public int blockCount;
	public String type;
	
	public Entity(double x,  double y, double z, int size, String type)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockCount = size;
		this.type = type;
	}
	
	@Override
	public String toString()
	{
		return x + ", " + y + ", " + z + " (" + blockCount + " blocks)";
	}
	
	public double distanceTo(Entity entity)
	{
		double dx = entity.x - this.x;
		double dy = entity.y - this.y;
		double dz = entity.z - this.z;
		
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
}