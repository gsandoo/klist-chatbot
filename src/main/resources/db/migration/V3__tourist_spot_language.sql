ALTER TABLE tourist_spot ADD COLUMN language VARCHAR(2) NOT NULL DEFAULT 'ko';
ALTER TABLE tourist_spot ADD CONSTRAINT ck_tourist_spot_language CHECK (language IN ('ko', 'en'));
ALTER TABLE tourist_spot DROP CONSTRAINT uk_tourist_spot_tour_api_content_id;
ALTER TABLE tourist_spot ADD CONSTRAINT uk_tourist_spot_tour_api_content_id UNIQUE (language, tour_api_content_id);
