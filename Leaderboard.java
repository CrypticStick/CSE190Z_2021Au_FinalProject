import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Leaderboard {

    public static final int MAX_LENGTH = 10;
    File leaderboardFile;
    private Document leaderboardDoc;
    private Element root;

    /**
     * Loads the leaderboard file.
     * @param trackId
     */
    public Leaderboard() {
        try {
            leaderboardFile = new File("leaderboards.xml");
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (!leaderboardFile.exists()) {
                leaderboardDoc = docBuilder.newDocument();
                root = leaderboardDoc.createElement("leaderboards");
                leaderboardDoc.appendChild(root);
            } else {
                leaderboardDoc = docBuilder.parse(leaderboardFile);
                root = leaderboardDoc.getDocumentElement();
            }
            leaderboardDoc.getDocumentElement().normalize();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.println("Can't load the leaderboards!");
            e.printStackTrace();
        }
    }

    /**
     * Gets the track's leaderboard element.
     * 
     * @param trackId the track's ID.
     * @return the track's leaderboard element (or null, if one doesn't exist).
     */
    private Element getLeaderboardElement(String trackId) {
        Element trackLeaderboard = null;
        NodeList boardList = root.getElementsByTagName("leaderboard");
        for (int i = 0; i < boardList.getLength(); i++) {
            Node element = boardList.item(i);
            if (element != null && element.getNodeType() == Node.ELEMENT_NODE) {
                String locTrackId = ((Element)element).getAttribute("trackId");
                if (locTrackId.equals(trackId)) {
                    trackLeaderboard = (Element)element;
                    break;
                }
            }
        }
        return trackLeaderboard;
    }

    /**
     * Creates a new leaderboard element for the given track.
     * 
     * @param trackId the track's ID.
     * @return the track's leaderboard element.
     */
    private Element createLeaderboardElement(String trackId) {
        Element trackLeaderboard = leaderboardDoc.createElement("leaderboard");
        trackLeaderboard.setAttribute("trackId", trackId);
        return trackLeaderboard;
    }

    /**
     * Adds the given entry to the leaderboard, shifting ranks as necessary.
     * @param entry
     */
    public void saveEntry(String trackId, LeaderboardEntry entry) {
        List<LeaderboardEntry> entries = getLeaderboard(trackId);
        boolean shiftedEntry = false;
        for (int i = 0; i < entries.size(); i++) {
            if (shiftedEntry) {
                entries.get(i).setRank(i + 1);
            } else if (entries.get(i).getTime() > entry.getTime()) {
                entry.setRank(i + 1);
                entries.add(i, entry);
                shiftedEntry = true;
            }
        }
        // Remove last entry from list if new one was insterted.
        if (shiftedEntry && entries.size() > MAX_LENGTH) {
            entries.remove(entries.size() - 1);
        // Add entry to end of list if list is short enough.
        } else if (!shiftedEntry && entries.size() < MAX_LENGTH) {
            entry.setRank(entries.size() + 1);
            entries.add(entry);
        }
        saveLeaderboard(trackId, entries);
    }

    private Node createEntry(LeaderboardEntry entry) {
        // Create new node
        Element entryNode = leaderboardDoc.createElement("entry");

        // Set rank attribute
        entryNode.setAttribute("rank", String.valueOf(entry.getRank()));

        // Create name element
        Element nameNode = leaderboardDoc.createElement("name");
        nameNode.appendChild(leaderboardDoc.createTextNode(entry.getName()));
        entryNode.appendChild(nameNode);

        //Create time element
        Element timeNode = leaderboardDoc.createElement("time");
        timeNode.appendChild(leaderboardDoc.createTextNode(String.valueOf(entry.getTime())));
        entryNode.appendChild(timeNode);

        return entryNode;
    }

    /**
     * Saves a new or updated leaderboard.
     * 
     * @param trackId the track's ID.
     * @param leaderboard the leaderboard data to save.
     */
    public void saveLeaderboard(String trackId, List<LeaderboardEntry> leaderboard) {
        Element oldBoardElement = getLeaderboardElement(trackId);
        if (oldBoardElement != null) {
            root.removeChild(oldBoardElement);
        }
        Element newBoardElement = createLeaderboardElement(trackId);
        // Loop through every entry in the document.
        for (int i = 0; i < leaderboard.size(); i++) {
            newBoardElement.appendChild(createEntry(leaderboard.get(i)));
        }
        root.appendChild(newBoardElement);
        updateFile();
    }

    /**
     * Saves the current leaderboard data to the disk.
     */
    private void updateFile() {
        try {
            // Instantiate new transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(leaderboardDoc);

            // Save to file.
            StreamResult leaderFileStream = new StreamResult(leaderboardFile);
            transformer.transform(source, leaderFileStream);
            
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            System.err.println("Failed to save leaderboard data!");
            e.printStackTrace();
        }
    }

    /**
     * Gets a list of entries for the given track's leaderboard.
     * 
     * @param trackId the track's ID.
     * @return a list of entries for this leaderboard.
     */
    public List<LeaderboardEntry> getLeaderboard(String trackId) {
        ArrayList<LeaderboardEntry> leaderboardMap = new ArrayList<>(MAX_LENGTH);

        Element boardElement = getLeaderboardElement(trackId);
        if (boardElement != null) {
            NodeList positionList = boardElement.getElementsByTagName("entry");
            // Loop through every entry in the document.
            for (int i = 0; i < positionList.getLength(); i++) {
                Node element = positionList.item(i);
                // If entry exists:
                if (element != null && element.getNodeType() == Node.ELEMENT_NODE) {
                    String entryRank = ((Element)element).getAttribute("rank");
                    // If rank is given:
                    if (!entryRank.isEmpty()) {
                        int rankNumber = Integer.parseInt(entryRank);
                        // If rank is high enough:
                        if (Integer.parseInt(entryRank) <= MAX_LENGTH) {
                            Element entry = (Element)element;
                            Node nameNode = entry.getElementsByTagName("name").item(0);
                            Node timeNode = entry.getElementsByTagName("time").item(0);
                            // Add entry to list!
                            LeaderboardEntry leadEntry = new LeaderboardEntry(
                                rankNumber, 
                                nameNode.getTextContent(), 
                                Long.parseLong(timeNode.getTextContent())
                            );
                            if (leaderboardMap.size() >= rankNumber) {
                                leaderboardMap.add(rankNumber-1, leadEntry);
                            } else {
                                leaderboardMap.add(leadEntry);
                            }
                        }
                    }
                }
            }
        }
        return leaderboardMap;
    }
}
