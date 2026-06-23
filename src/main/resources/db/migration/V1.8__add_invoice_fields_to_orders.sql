-- Campos para el flujo de facturación solicitada por el chatbot.
-- requires_invoice: bandera que activa el badge en backoffice.
-- billing_cuit:     CUIT al que se debe emitir la factura (nulo hasta que se solicite).
ALTER TABLE orders.orders
    ADD COLUMN requires_invoice BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN billing_cuit     VARCHAR(20);
