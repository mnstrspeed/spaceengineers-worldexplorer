import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Program 
{
	public static void main(String[] args)
	{
		File saveFile = getSaveFile();
		if (saveFile == null)
			System.exit(0);
		
		List<Entity> entities;
		try 
		{
			entities = getEntities(saveFile);
		} 
		catch (SAXException | IOException | ParserConfigurationException e) 
		{
			JOptionPane.showMessageDialog(null, "Couldn't load save game: " + e.getMessage());
			return;
		}
		Entity player = extractPlayerEntity(entities);
		
		// Just for fun
		Collections.sort(entities, (a, b) -> 
			Double.compare(a.distanceTo(player), b.distanceTo(player)));
		System.out.println("Player is at " + player);
		System.out.println("Closest objects:");
		for (Entity entity : entities)
			System.out.println(entity);
		
		JFrame frame = new JFrame();
		frame.setTitle("Space Engineers World Explorer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1280, 720);
		frame.setContentPane(new SpacePanel(frame, entities, player));
		frame.setVisible(true);
	}

	public static List<Entity> getEntities(File saveFile) throws SAXException,
			IOException, ParserConfigurationException 
	{
		ArrayList<Entity> entities = new ArrayList<Entity>();
		
		Document document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(saveFile);
		
		NodeList entityNodes = document.getElementsByTagName("MyObjectBuilder_EntityBase");
		for (int i = 0; i < entityNodes.getLength(); i++)
		{
			Element entityNode = (Element)entityNodes.item(i);
			
			int blockCount = entityNode.getElementsByTagName("MyObjectBuilder_CubeBlock").getLength();
			String type = entityNode.getAttributes().getNamedItem("xsi:type").getTextContent();
			
			NodeList positionNodes = entityNode.getElementsByTagName("Position");
			if (positionNodes.getLength() > 0)
			{
				Node positionNode = positionNodes.item(0);
				entities.add(new Entity(
						Double.parseDouble(positionNode.getAttributes().getNamedItem("x").getTextContent()), 
						Double.parseDouble(positionNode.getAttributes().getNamedItem("y").getTextContent()),
						Double.parseDouble(positionNode.getAttributes().getNamedItem("z").getTextContent()), 
						blockCount, type));
			}
		}
		return entities;
	}
	
	public static Entity extractPlayerEntity(List<Entity> entities)
	{
		Iterator<Entity> iterator = entities.iterator();
		while (iterator.hasNext())
		{
			Entity current = iterator.next();
			if (current.type.equals("MyObjectBuilder_Character"))
			{
				iterator.remove();
				return current;
			}
		}
		return null;
	}

	private static File getSaveFile() 
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Space Engineers save file", "sbs"));
		
		File savesDirectory = new File(System.getenv("APPDATA") + "/SpaceEngineers/Saves");
		if (savesDirectory.isDirectory())
			fileChooser.setCurrentDirectory(savesDirectory);
		
		return fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? 
				fileChooser.getSelectedFile() : null;
	}
}
