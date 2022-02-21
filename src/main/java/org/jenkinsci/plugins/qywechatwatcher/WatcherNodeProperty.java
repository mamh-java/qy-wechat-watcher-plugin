package org.jenkinsci.plugins.qywechatwatcher;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;


public class WatcherNodeProperty extends NodeProperty<Node> {
    private static final Logger LOGGER = Logger.getLogger(WatcherNodeProperty.class.getName());

    private final String onlineAddresses;
    private final String offlineAddresses;

    @DataBoundConstructor
    public WatcherNodeProperty(final String onlineAddresses, final String offlineAddresses) {
        this.onlineAddresses = onlineAddresses;
        this.offlineAddresses = offlineAddresses;
    }

    public String getOnlineAddresses() {
        return onlineAddresses;
    }

    public String getOfflineAddresses() {
        return offlineAddresses;
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {

        public static final String OFFLINE_ADDRESSES = "offlineAddresses";
        public static final String ONLINE_ADDRESSES = "onlineAddresses";

        @Override
        public boolean isApplicable(Class<? extends Node> nodeType) {

            return true;
        }

        @Override
        public NodeProperty<?> newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            final String onlineAddresses = formData.getString(ONLINE_ADDRESSES);
            final String offlineAddresses = formData.getString(OFFLINE_ADDRESSES);

            assert onlineAddresses != null;
            assert offlineAddresses != null;

            if (onlineAddresses.isEmpty() && offlineAddresses.isEmpty()) return null;

            return new WatcherNodeProperty(onlineAddresses, offlineAddresses);
        }

        public FormValidation doCheckOnlineAddresses(@QueryParameter String value) {
            LOGGER.info("doCheckOnlineAddresses: " + value);
            return FormValidation.ok();
        }

        public FormValidation doCheckOfflineAddresses(@QueryParameter String value) {
            LOGGER.info("doCheckOfflineAddresses: " + value);
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Qywechat when Node online status changes";
        }
    }
}
