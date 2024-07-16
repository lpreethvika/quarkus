package io.quarkus.tls;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.inject.Singleton;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.tls.runtime.CertificateRecorder;
import io.quarkus.tls.runtime.config.TlsConfig;
import io.quarkus.vertx.deployment.VertxBuildItem;

public class CertificatesProcessor {

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public TlsRegistryBuildItem initializeCertificate(
            TlsConfig config, Optional<VertxBuildItem> vertx, CertificateRecorder recorder,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
            List<TlsCertificateBuildItem> otherCertificates,
            ShutdownContextBuildItem shutdown) {

        if (vertx.isPresent()) {
            recorder.validateCertificates(config, vertx.get().getVertx(), shutdown);
        }

        for (TlsCertificateBuildItem certificate : otherCertificates) {
            recorder.register(certificate.name, certificate.supplier);
        }

        Supplier<TlsConfigurationRegistry> supplier = recorder.getSupplier();

        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(TlsConfigurationRegistry.class)
                .supplier(supplier)
                .scope(Singleton.class)
                .unremovable()
                .setRuntimeInit();

        syntheticBeans.produce(configurator.done());

        return new TlsRegistryBuildItem(supplier);
    }

}
