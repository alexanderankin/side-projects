package main

import (
	"context"
	"flag"
	"github.com/hashicorp/terraform-plugin-framework/datasource"
	datasource_schema "github.com/hashicorp/terraform-plugin-framework/datasource/schema"
	"github.com/hashicorp/terraform-plugin-framework/provider"
	provider_schema "github.com/hashicorp/terraform-plugin-framework/provider/schema"
	"github.com/hashicorp/terraform-plugin-framework/providerserver"
	"github.com/hashicorp/terraform-plugin-framework/resource"
	"log"
)

func main() {
	var debug bool

	flag.BoolVar(&debug, "debug", false, "set to true to run the provider with support for debuggers like delve")
	flag.Parse()

	opts := providerserver.ServeOpts{
		// NOTE: This is not a typical Terraform Registry provider address,
		// such as registry.terraform.io/hashicorp/hashicups. This specific
		// provider address is used in these tutorials in conjunction with a
		// specific Terraform CLI configuration for manual development testing
		// of this provider.
		Address: "hashicorp.com/edu/hashicups",
		Debug:   debug,
	}

	err := providerserver.Serve(context.Background(), New("0.0.1"), opts)

	if err != nil {
		log.Fatal(err.Error())
	}
}

// Ensure the implementation satisfies the expected interfaces.
var (
	_ provider.Provider = &hashicupsProvider{}
)

// New is a helper function to simplify provider server and testing implementation.
func New(version string) func() provider.Provider {
	return func() provider.Provider {
		return &hashicupsProvider{
			version: version,
		}
	}
}

// hashicupsProvider is the provider implementation.
type hashicupsProvider struct {
	// version is set to the provider version on release, "dev" when the
	// provider is built and ran locally, and "test" when running acceptance
	// testing.
	version string
}

// Metadata returns the provider type name.
func (p *hashicupsProvider) Metadata(_ context.Context, _ provider.MetadataRequest, resp *provider.MetadataResponse) {
	resp.TypeName = "basic"
	resp.Version = p.version
}

// Schema defines the provider-level schema for configuration data.
func (p *hashicupsProvider) Schema(_ context.Context, _ provider.SchemaRequest, resp *provider.SchemaResponse) {
	resp.Schema = provider_schema.Schema{
		//Attributes: map[string]provider_schema.Attribute{
		//	"id":        schema.StringAttribute{Computed: true},
		//	"some_data": schema.StringAttribute{Computed: true},
		//},
	}
}

// Configure prepares a HashiCups API client for data sources and resources.
func (p *hashicupsProvider) Configure(ctx context.Context, req provider.ConfigureRequest, resp *provider.ConfigureResponse) {
}

// ensure implements
var _ datasource.DataSource = &exampleDs{}

type exampleDs struct {
}

func (e exampleDs) Metadata(ctx context.Context, request datasource.MetadataRequest, response *datasource.MetadataResponse) {
	//f, _ := os.OpenFile("/tmp/log", os.O_APPEND|os.O_WRONLY, 0644)
	//f.WriteString("request.ProviderTypeName")
	//f.WriteString(request.ProviderTypeName)
	//f.Close()
	//response.TypeName = request.ProviderTypeName + "_example"
	//response.TypeName = "basic_provider_example"
	response.TypeName = "basic_example"
	//response.TypeName = "basic-plugin-provider_basic_example"
}

func (e exampleDs) Schema(ctx context.Context, request datasource.SchemaRequest, response *datasource.SchemaResponse) {
	response.Schema = datasource_schema.Schema{
		Attributes: map[string]datasource_schema.Attribute{
			"id":        datasource_schema.StringAttribute{Optional: true},
			"some_data": datasource_schema.StringAttribute{Computed: true},
		},
	}
}

func (e exampleDs) Read(ctx context.Context, request datasource.ReadRequest, response *datasource.ReadResponse) {
	//basetypes.NewMapUnknown(basetypes.MapType{})
	//
	//state := basetypes.ObjectType{
	//	AttrTypes: map[string]attr.Type{
	//		"id":        basetypes.Int64Type{},
	//		"some_data": basetypes.StringType{},
	//	},
	//}
	data := map[string]interface{}{
		"id":        1,
		"some_data": "some data here",
	}
	//
	//response.Diagnostics.Append(response.State.Set(ctx, state)...)
	//if response.Diagnostics.HasError() {
	//	return
	//}
	response.State.Set(ctx, data)
}

func newExampleDs() datasource.DataSource {
	return &exampleDs{}
}

// DataSources defines the data sources implemented in the provider.
func (p *hashicupsProvider) DataSources(_ context.Context) []func() datasource.DataSource {
	return []func() datasource.DataSource{newExampleDs}
}

// Resources defines the resources implemented in the provider.
func (p *hashicupsProvider) Resources(_ context.Context) []func() resource.Resource {
	return nil
}
