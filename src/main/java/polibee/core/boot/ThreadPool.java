package polibee.core.boot;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Thread Pool service will help you to manage Spring Task Executor and help with grateful exit
 */
@Service
public class ThreadPool implements ApplicationListener<ContextClosedEvent> {
    public static final String THREAD_POOL_TASK_EXECUTOR_CORE_POOL_SIZE = "task_executor.core_pool_size";
    public static final String THREAD_POOL_TASK_EXECUTOR_MAX_POOL_SIZE = "task_executor.max_pool_size";
    public static final String THREAD_POOL_TASK_EXECUTOR_QUEUE_CAPACITY = "task_executor.queue_capacity";
    public static final String THREAD_POOL_TASK_EXECUTOR_THREAD_NAME_PREFIX = "task_executor.thread_name_prefix";

    public static final String THREAD_POOL_TASK_SCHEDULER_POOL_SIZE = "scheduler.pool_size";
    public static final String THREAD_POOL_TASK_SCHEDULER_THREAD_NAME_PREFIX = "scheduler.thread_name_prefix";

    private AnnotationConfigApplicationContext _context;
    private List<ExecutorConfigurationSupport> _executors = new ArrayList<>();

    private List<Consumer<String>> _afterExit = new ArrayList<>();
    private List<Consumer<String>> _beforeExit = new ArrayList<>();

    private static Logger _logger = LogManager.getLogger(ThreadPool.class);

    private int _max_wait_till_terminated;

    @Autowired
    public ThreadPool(AnnotationConfigApplicationContext context) {
        this._context = context;
        ConfigHandler configHandler = _context.getBean(ConfigHandler.class);
        try {
            _max_wait_till_terminated = Integer.parseInt(
                    configHandler.getConfig("GRATEFUL_MAX_WAIT_TILL_TERMINATED", "120"));
        } catch (InvalidConfigException | NumberFormatException e) {
            _max_wait_till_terminated = 120;
        }
    }

    /**
     * Create default async executor
     */
    public ThreadPoolTaskExecutor createAsyncExecutor() throws InvalidConfigException {
        ConfigHandler configHandler = _context.getBean(ConfigHandler.class);

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        try {
            int corePoolSize =
                    Integer.parseInt(configHandler.getConfig(THREAD_POOL_TASK_EXECUTOR_CORE_POOL_SIZE, "5"));
            int maxPoolSize =
                    Integer.parseInt(configHandler.getConfig(THREAD_POOL_TASK_EXECUTOR_MAX_POOL_SIZE, "10"));
            int queueCapacity =
                    Integer.parseInt(configHandler.getConfig(THREAD_POOL_TASK_EXECUTOR_QUEUE_CAPACITY, "500"));
            String namePrefix = configHandler.getConfig(THREAD_POOL_TASK_EXECUTOR_THREAD_NAME_PREFIX, "task-executor-");
            threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
            threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
            threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
            threadPoolTaskExecutor.setThreadNamePrefix(namePrefix);
            threadPoolTaskExecutor.initialize();
        } catch (NumberFormatException ex) {
            throw new InvalidConfigException("Async task executor properties is not valid", ex);
        }
        _executors.add(threadPoolTaskExecutor);
        return threadPoolTaskExecutor;
    }

    /**
     * Get default task scheduler executor for register
     */
    public ThreadPoolTaskScheduler createSchedulingTaskExecutor() throws InvalidConfigException {
        ConfigHandler configHandler = _context.getBean(ConfigHandler.class);

        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        try {
            int poolSize =
                    Integer.parseInt(configHandler.getConfig(THREAD_POOL_TASK_SCHEDULER_POOL_SIZE, "5"));
            String namePrefix = configHandler.getConfig(THREAD_POOL_TASK_SCHEDULER_THREAD_NAME_PREFIX, "task-scheduler-");
            threadPoolTaskScheduler.setPoolSize(poolSize);
            threadPoolTaskScheduler.setThreadNamePrefix(namePrefix);
            threadPoolTaskScheduler.initialize();
        } catch (NumberFormatException ex) {
            throw new InvalidConfigException("Task scheduler executor properties is not valid", ex);
        }
        _executors.add(threadPoolTaskScheduler);
        return threadPoolTaskScheduler;
    }

    /**
     * Handle grateful exit
     *
     * @param contextClosedEvent sự kiện context bị close
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        System.out.println("context closed event");
        for (ExecutorConfigurationSupport executor : _executors) {
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.shutdown();
        }

        for (ExecutorConfigurationSupport executor : _executors) {
            try {
                if (executor instanceof ThreadPoolTaskExecutor) {
                    ((ThreadPoolTaskExecutor) executor)
                            .getThreadPoolExecutor().awaitTermination(_max_wait_till_terminated, TimeUnit.SECONDS);
                } else if (executor instanceof ThreadPoolTaskScheduler) {
                    ((ThreadPoolTaskScheduler) executor)
                            .getScheduledThreadPoolExecutor().awaitTermination(_max_wait_till_terminated, TimeUnit.SECONDS);
                }
            } catch (IllegalStateException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Thoát app một cách nhẹ nhàng, đóng hết các executor trước khi thoát và chờ các task đang thực hiện được thực hiện xong
     */
    public void gratefulExitApplication() {
        FutureTask<String> futureTask1 = new FutureTask<String>(new ExitApp());
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(futureTask1);
    }

    /**
     * Thêm một Executor Configuration Support vào Thread Pool quản lý
     * @param executor Executor cần quản lý
     */
    public void addExecutor(ExecutorConfigurationSupport executor) {
        this._executors.add(executor);
    }

    /**
     * Thêm một hàm xử lý sau khi thoát ứng dụng, đóng context xong
     * @param afterExit hàm xử lý sau khi đóng context
     */
    public void addAfterExit(Consumer<String> afterExit) {
        _afterExit.add(afterExit);
    }

    /**
     * Thêm một hàm xử lý trước khi thoát ứng dụng, đóng context
     * @param beforeExit hàm xử lý trước khi đóng context
     */
    public void addBeforeExit(Consumer<String> beforeExit) {
        _beforeExit.add(beforeExit);
    }

    /**
     * Callable phục vụ việc thoát App ở trên một thread khác, executor riêng nhằm các executor được sinh ra bởi hệ thống
     * sẽ được shutdown và terminate kịp thời
     */
    class ExitApp implements Callable<String> {
        @Override
        public String call() throws Exception {
            for (Consumer<String> exitFunction: _beforeExit) {
                exitFunction.accept("");
            }

            _context.close();
            if (_logger.isInfoEnabled()) {
                _logger.info("The application will be now quit after all context has been closed");
            }

            for (Consumer<String> exitFunction: _afterExit) {
                exitFunction.accept("");
            }

            System.exit(0);
            return null;
        }
    }
}
