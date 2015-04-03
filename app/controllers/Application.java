package controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Application extends Controller {
	public static Result index(String number) {
		return ok(index.render("This thing works: " + number));
	}
	
	public static Result x() {
		SpreadsheetService service = new SpreadsheetService("DaBadassProject");
		try {
			service.setUserCredentials(Credentials.userName, Credentials.password);
		} catch (AuthenticationException e1) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e1.printStackTrace(ps);
			Logger.error(baos.toString());
		}
		
		Logger.info("Starting");
		URL SPREADSHEET_FEED_URL;
	
		try {
			SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
		
			
			
			//Get the spreadsheet
			final SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
			final List<SpreadsheetEntry> spreadsheets = feed.getEntries();
			SpreadsheetEntry ss = null;
			boolean initialized = false;
			for (final SpreadsheetEntry spreadsheet : spreadsheets) {
				if (spreadsheet.getTitle().getPlainText().equals("Badasses")) {
					ss = spreadsheet;
					break;
				}
			}
			if (ss==null) {
				return badRequest("Fail");
			}
			Logger.info("Page " + ss.getTitle().getPlainText() + " found");
			
			
			//Get the worksheet
			final WorksheetFeed worksheetFeed = service.getFeed(ss.getWorksheetFeedUrl(), WorksheetFeed.class);
			final List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
			final WorksheetEntry ws = worksheets.get(0);
			
			Logger.info("Sheet " + ws.getTitle().getPlainText() + " found");
			
			//Get the fields of the first line and increment counter in B1
			final URL cellFeedUrl = new URI(ws.getCellFeedUrl().toString()
			        + "?min-row=1&min-col=1").toURL();
			final CellFeed cellFeed = service.getFeed(ws.getCellFeedUrl(), CellFeed.class);
			final List<CellEntry> cells = cellFeed.getEntries();
			
			for (final CellEntry c : cells) {
				final String cindex = c.getId().substring(c.getId().lastIndexOf('/') + 1);
				Logger.info("[" + cindex + "]" + c.getPlainTextContent());
				if (cindex.equals("R1C2")) {
					c.changeInputValueLocal((Integer.parseInt(c.getPlainTextContent())+1)+"");
					c.update();
				}
			}
			
			
			/*
			List<SpreadsheetEntry> spreadsheets = feed.getEntries();
			
			// Iterate through all of the spreadsheets returned
			for (SpreadsheetEntry spreadsheet : spreadsheets) {
		    	// Print the title of this spreadsheet to the screen
		    	//System.out.println(spreadsheet.getTitle().getPlainText());
		    	Logger.info(spreadsheet.getTitle().getPlainText());
			}*/	
		} catch (IOException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			Logger.error(baos.toString());
		} catch (ServiceException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			Logger.error(baos.toString());
		} catch (URISyntaxException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			Logger.error(baos.toString());
		}
	  
		return ok(index.render("This thing works: " + 456));
	}
}