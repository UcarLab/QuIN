package upkeep;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class OldDataRemoverExecutor implements ServletContextListener{

    private ScheduledExecutorService _executor;
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		_executor = Executors.newSingleThreadScheduledExecutor();
		_executor.scheduleAtFixedRate(new OldDataRemover(), 0, 24, TimeUnit.HOURS);
	}
    
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		_executor.shutdown();
	}



}
