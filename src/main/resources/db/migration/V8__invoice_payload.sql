alter table telegram.invoices add column payload text;

create index invoices_payload_idx
    on telegram.invoices (payload)
    where payload is not null;

create index successful_payments_invoice_payload_idx
    on telegram.successful_payments (invoice_payload);
