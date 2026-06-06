package properties

import "encoding/json"

type Config struct {
	Server struct {
		Address string `mapstructure:"address"`
	} `mapstructure:"server"`

	Healthcheck struct {
		Path string `mapstructure:"path"`
	} `mapstructure:"healthcheck"`

	TestRequestUrl struct {
		Path string `mapstructure:"path"`
		URL  string `mapstructure:"url"`
	} `mapstructure:"testRequest"`

	Logging struct {
		Level  string `mapstructure:"level"`
		Format string `mapstructure:"format"`
	} `mapstructure:"logging"`
}

func (c *Config) MustAsMap() map[string]interface{} {
	asMap, err := c.AsMap()
	if err != nil {
		panic(err)
	}
	return asMap
}

func (c *Config) AsMap() (map[string]interface{}, error) {
	b, err := json.Marshal(c)
	if err != nil {
		return nil, err
	}

	var m map[string]interface{}
	if err := json.Unmarshal(b, &m); err != nil {
		return nil, err
	}

	return m, nil
}
