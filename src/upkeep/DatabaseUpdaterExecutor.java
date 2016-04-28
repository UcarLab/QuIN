package upkeep;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DatabaseUpdaterExecutor implements ServletContextListener{

   // private ScheduledExecutorService _executor;
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		//_executor = Executors.newSingleThreadScheduledExecutor();
		//_executor.scheduleAtFixedRate(new DatabaseUpdater(), 0, 30, TimeUnit.DAYS);
	}
    
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		//_executor.shutdown();
		
	}

}
