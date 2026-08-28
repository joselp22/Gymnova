--
-- PostgreSQL database dump
--

\restrict fLwAsoMOivs6ceSkDIzZEcnKmbEBC9AenJEYAa139caUmWba0yScxUL7Dnv2fBp

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: factura; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.factura (id_factura, numero_factura, fecha_emision, total_descuento, impuesto, estado_factura, id_cliente) FROM stdin;
1	FAC-00002	2026-08-11	0.00	5.40	PAGADA	3
2	FAC-00003	2026-08-11	0.00	5.40	PAGADA	4
3	FAC-00001	2026-08-11	0.00	5.40	PAGADA	2
4	FAC-00004	2026-08-11	0.00	5.40	PAGADA	5
\.


--
-- Data for Name: pago; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pago (id_pago, monto_recibido, monto_pago, codigo_pago, fecha_hora_pago, estado_pago, referencia_transaccion, id_factura, id_metodo_pago) FROM stdin;
1	45.00	45.00	PG00002	2026-08-12 08:14:25.445945	CONFIRMADO	TRX-CL00002	1	1
2	45.00	45.00	PG00003	2026-08-12 08:14:25.445945	CONFIRMADO	TRX-CL00003	2	1
3	45.00	45.00	PG00001	2026-08-12 08:14:25.445945	CONFIRMADO	TRX-CL00001	3	1
4	45.00	45.00	PG00004	2026-08-12 08:14:25.445945	CONFIRMADO	TRX-CL00004	4	1
\.


--
-- Data for Name: comprobante; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.comprobante (id_comprobante, numero_comprobante, tipo_comprobante, fecha_emision, formato_archivo, ruta_archivo, correo_envio, fecha_envio, estado_comprobante, id_pago) FROM stdin;
1	COMP-00002	RECIBO	2026-08-12	PDF	\N	\N	\N	GENERADO	1
2	COMP-00003	RECIBO	2026-08-12	PDF	\N	\N	\N	GENERADO	2
3	COMP-00001	RECIBO	2026-08-12	PDF	\N	\N	\N	GENERADO	3
4	COMP-00004	RECIBO	2026-08-12	PDF	\N	\N	\N	GENERADO	4
\.


--
-- Data for Name: detalle_factura; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.detalle_factura (id_detalle_factura, tipo_concepto, codigo_referencia, cantidad, precio_unitario, porcentaje_impuesto, id_factura) FROM stdin;
1	MEMBRESIA	MB00002	1	45.00	12.00	1
2	MEMBRESIA	MB00003	1	45.00	12.00	2
3	MEMBRESIA	MB00001	1	45.00	12.00	3
4	MEMBRESIA	MB00004	1	45.00	12.00	4
\.


--
-- Name: comprobante_id_comprobante_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.comprobante_id_comprobante_seq', 8, true);


--
-- Name: detalle_factura_id_detalle_factura_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_factura_id_detalle_factura_seq', 4, true);


--
-- Name: factura_id_factura_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.factura_id_factura_seq', 8, true);


--
-- Name: pago_id_pago_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.pago_id_pago_seq', 8, true);


--
-- PostgreSQL database dump complete
--

\unrestrict fLwAsoMOivs6ceSkDIzZEcnKmbEBC9AenJEYAa139caUmWba0yScxUL7Dnv2fBp

