package zoo;

import exception.ZooException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper.States;

public interface ZooServer extends Watcher {
    String connectToZooKeeper() throws ZooException;
    String disconnectFromZooKeeper() throws ZooException;
    String connectToCluster() throws ZooException;
    String disconnectFromCluster() throws ZooException;
    String getClusterInfo();
    States getState();
    boolean isConnected();
}
